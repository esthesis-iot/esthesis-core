package esthesis.service.agent.impl.service;

import static esthesis.core.common.AppConstants.GridFS.PROVISIONING_BUCKET_NAME;
import static esthesis.core.common.AppConstants.GridFS.PROVISIONING_METADATA_NAME;
import static esthesis.core.common.AppConstants.HARDWARE_ID_REGEX;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.common.crypto.dto.SignatureVerificationRequestDTO;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QLimitException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QSecurityException;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.core.common.AppConstants.Provisioning.Redis;
import esthesis.core.common.AppConstants.Provisioning.Type;
import esthesis.service.agent.dto.AgentProvisioningInfoResponse;
import esthesis.service.common.gridfs.GridFSDTO;
import esthesis.service.common.gridfs.GridFSService;
import esthesis.service.crypto.resource.CASystemResource;
import esthesis.service.crypto.resource.SigningSystemResource;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.infrastructure.resource.InfrastructureMqttSystemResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.resource.ProvisioningSystemResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsSystemResource;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.runtime.util.HashUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service for handling agent operations.
 */
@Slf4j
@ApplicationScoped
public class AgentService {

	@ConfigProperty(name = "quarkus.mongodb.database")
	String dbName;

	@Inject
	@RestClient
	CASystemResource caSystemResource;

	@Inject
	@RestClient
	SettingsSystemResource settingsSystemResource;

	@Inject
	@RestClient
	InfrastructureMqttSystemResource infrastructureMqttSystemResource;

	@Inject
	@RestClient
	DeviceSystemResource deviceSystemResource;

	@Inject
	@RestClient
	ProvisioningSystemResource provisioningAgentResource;

	@Inject
	@RestClient
	SigningSystemResource signingSystemResource;

	@Inject
	RedisUtils redisUtils;

	@Inject
	GridFSService gridFSService;

	// The number of seconds after which the counter for provisioning requests is reset (in seconds).
	private static final int REQUEST_COUNTER_TIMEOUT = 300;

	// The number of provisioning requests that can be made within the timeout period.
	private static final int REQUESTS_PER_TIMESLOT = 5;

	/**
	 * Attempts to validate a request token sent by a device while requesting information on available
	 * provisioning packages. The token is an RSA/SHA256 digital signature of a SHA256 hashed version
	 * of the hardware id of the device requesting the information.
	 *
	 * @param hardwareId The hardware id of the device requesting the information.
	 * @param token      The token sent by the device.
	 */
	private void validateRequestToken(String hardwareId, Optional<String> token) {
		if (token.isEmpty()) {
			throw new QSecurityException(
				"Requesting provisioning package information for hardware id '{}' requires a token.",
				hardwareId);
		}

		// Obtain the necessary information to verify the signature.
		String publicKey = deviceSystemResource.findPublicKey(hardwareId);
		String keyAlgorithm = settingsSystemResource.findByName(
			NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM).asString();
		String signatureAlgorithm = settingsSystemResource.findByName(
			NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM).asString();

		// Set up the signature verification request.
		SignatureVerificationRequestDTO request = new SignatureVerificationRequestDTO();
		request.setPublicKey(publicKey);
		request.setKeyAlgorithm(keyAlgorithm);
		request.setPayload(HashUtil.sha256(hardwareId).getBytes(StandardCharsets.UTF_8));
		request.setPayload(hardwareId.getBytes(StandardCharsets.UTF_8));
		request.setSignature(token.get());
		request.setSignatureAlgorithm(signatureAlgorithm);

		// Verify the signature.
		try {
			signingSystemResource.verifySignature(request);
		} catch (Exception e) {
			throw new QSecurityException("Invalid request token '{}' for hardware id '{}'.", token.get(),
				hardwareId);
		}

		log.debug("Validating find provisioning package request token '{}' for hardware id '{}'.",
			token, hardwareId);
	}

	/**
	 * Validates that the number of requests for provisioning packages for a device with a given
	 * hardware id does not exceed the limit set by the system. The limit is defined by the number of
	 * requests that can be made within a certain time period.
	 *
	 * @param hardwareId The hardware id of the device for which the requests are being made.
	 */
	private void validateRequestsLimit(String hardwareId) {
		if (settingsSystemResource.findByName(NamedSetting.DEVICE_PROVISIONING_SECURE).asBoolean()) {
			long counter = redisUtils.incrCounter(KeyType.ESTHESIS_PRT, hardwareId,
				AgentService.REQUEST_COUNTER_TIMEOUT);
			if (counter > AgentService.REQUESTS_PER_TIMESLOT) {
				throw new QLimitException(
					"Device with hardware id '{}' has exceeded the number of allowed provisioning "
						+ "requests, '{}' requests per '{}' seconds).", hardwareId,
					AgentService.REQUESTS_PER_TIMESLOT, AgentService.REQUEST_COUNTER_TIMEOUT);
			}

			// If the token was validated, reset the caching counter.
			redisUtils.resetCounter(KeyType.ESTHESIS_PRT, hardwareId);
		}
	}

	/**
	 * Prepares a response with the details of a provisioning package and a download token.
	 *
	 * @param pp The provisioning package to prepare the response for.
	 * @return The response with the provisioning package details and the download token.
	 */
	private AgentProvisioningInfoResponse prepareAgentProvisioningInfoResponse(
		ProvisioningPackageEntity pp) {
		log.debug("Found provisioning package '{}'.", pp);

		String randomToken = UUID.randomUUID().toString().replace("-", "");
		redisUtils.setToHash(KeyType.ESTHESIS_PPDT, randomToken, Redis.DOWNLOAD_TOKEN_PACKAGE_ID,
			pp.getId().toString());
		redisUtils.setToHash(KeyType.ESTHESIS_PPDT, randomToken, Redis.DOWNLOAD_TOKEN_CREATED_ON,
			Instant.now().toString());
		redisUtils.setExpirationForHash(KeyType.ESTHESIS_PPDT, randomToken,
			settingsSystemResource.findByName(NamedSetting.DEVICE_PROVISIONING_CACHE_TIME).asLong() * 60);

		// Prepare the reply with the provisioning package details and the download token.
		AgentProvisioningInfoResponse apir = new AgentProvisioningInfoResponse();
		apir.setId(pp.getId().toString());
		apir.setName(pp.getName());
		apir.setVersion(pp.getVersion());
		apir.setSize(pp.getSize());
		apir.setSha256(pp.getSha256());
		apir.setType(pp.getType());
		if (pp.getType() == Type.EXTERNAL) {
			apir.setDownloadUrl(pp.getUrl());
		} else if (pp.getType() == Type.INTERNAL) {
			apir.setDownloadToken(randomToken);
		}

		log.debug("Prepared provisioning package response '{}'.", apir);

		return apir;
	}

	/**
	 * Returns information of a provisioning package by its ID.
	 *
	 * @param hardwareId The hardware id of the device requesting the information.
	 * @param packageId  The id of the provisioning package to return information for.
	 * @param token      The token sent by the device.
	 * @return The response with the provisioning package details.
	 */
	public AgentProvisioningInfoResponse findProvisioningPackageById(String hardwareId,
		String packageId, Optional<String> token) {
		// Check that requests for this hardware id are not being made too often.
		validateRequestsLimit(hardwareId);

		// Check that the provided token (if provisioning is running in secure mode) is valid.
		if (settingsSystemResource.findByName(NamedSetting.DEVICE_PROVISIONING_SECURE).asBoolean()) {
			validateRequestToken(hardwareId, token);
		}

		ProvisioningPackageEntity pp = provisioningAgentResource.findById(packageId);

		// If a provisioning package was not found, return an empty response.
		if (pp == null) {
			log.warn("Provisioning package with id '{}' not found.", packageId);
			return new AgentProvisioningInfoResponse();
		} else {
			return prepareAgentProvisioningInfoResponse(pp);
		}
	}

	/**
	 * Returns information of a provisioning package that can be downloaded by this device.
	 * <p>
	 * This method maintains a counter of provisioning requests for each device and denies additional
	 * requests for the same device if more than a certain number of failed requests are made within a
	 * certain time period. The counter functionality only works when the platform is running in
	 * secure provisioning mode, and it is reset once a successful request is made.
	 *
	 * @param hardwareId The hardware id of the device requesting the information.
	 * @param version    The current version of the firmware installed on the device.
	 * @param token      The token sent by the device.
	 * @return The response with the provisioning package details.
	 */
	public AgentProvisioningInfoResponse findProvisioningPackage(String hardwareId, String version,
		Optional<String> token) {
		// Check that requests for this hardware id are not being made too often.
		validateRequestsLimit(hardwareId);

		// Check that the provided token (if provisioning is running in secure mode) is valid.
		validateRequestToken(hardwareId, token);

		// Find a candidate provisioning package.
		log.debug("Requesting provisioning info for device with hardware ID '{}'.", hardwareId);
		ProvisioningPackageEntity pp = provisioningAgentResource.find(hardwareId, version);

		// If a provisioning package was not found, return an empty response.
		if (pp == null) {
			return new AgentProvisioningInfoResponse();
		} else {
			return prepareAgentProvisioningInfoResponse(pp);
		}
	}

	/**
	 * Downloads a provisioning package by its download token.
	 *
	 * @param token The download token of the provisioning package to download.
	 * @return The binary data of the provisioning package.
	 */
	public Uni<byte[]> downloadProvisioningPackage(String token) {
		return redisUtils.getFromHashReactive(KeyType.ESTHESIS_PPDT, token,
			Redis.DOWNLOAD_TOKEN_PACKAGE_ID).onItem().ifNull().failWith(
			() -> new QDoesNotExistException("Invalid download token '{}' for provisioning package.",
				token)).onItem().transformToUni(id -> gridFSService.downloadBinary(
			GridFSDTO.builder().database(dbName).metadataName(PROVISIONING_METADATA_NAME)
				.metadataValue(id).bucketName(PROVISIONING_BUCKET_NAME).build()));
	}

	/**
	 * Registers a device with the system and returns the necessary information for the device to
	 * connect to the platform.
	 *
	 * @param agentRegistrationRequest The request with the details of the device to register.
	 * @return The response with the necessary information for the device to connect to the platform.
	 * @throws NoSuchAlgorithmException  If the algorithm used for key generation is not supported.
	 * @throws IOException               If an I/O error occurs.
	 * @throws InvalidKeySpecException   If the key specification is invalid.
	 * @throws NoSuchProviderException   If the security provider is not found.
	 * @throws OperatorCreationException If the operator for key generation cannot be created.
	 */
	@Transactional
	public AgentRegistrationResponse register(AgentRegistrationRequest agentRegistrationRequest)
	throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchProviderException,
				 OperatorCreationException {
		// Check the proposed hardware id conforms to the naming convention.
		if (!agentRegistrationRequest.getHardwareId().matches(HARDWARE_ID_REGEX)) {
			throw new QMismatchException("Hardware ID does not conform to the naming convention '{}'.",
				HARDWARE_ID_REGEX);
		}

		// Prepare a registration request.
		DeviceRegistrationDTO deviceRegistration = new DeviceRegistrationDTO();
		deviceRegistration.setHardwareId(agentRegistrationRequest.getHardwareId());
		deviceRegistration.setAttributes(agentRegistrationRequest.getAttributes());
		if (StringUtils.isNotBlank(agentRegistrationRequest.getTags())) {
			deviceRegistration.setTags(
				Arrays.stream(agentRegistrationRequest.getTags().split(",")).toList());
		}
		deviceRegistration.setType(agentRegistrationRequest.getType());
		if (StringUtils.isNotBlank(agentRegistrationRequest.getRegistrationSecret())) {
			deviceRegistration.setRegistrationSecret(agentRegistrationRequest.getRegistrationSecret());
		}

		deviceRegistration.setAttributes(agentRegistrationRequest.getAttributes());

		// Issue registration request.
		log.debug("Requesting device registration with: '{}'", deviceRegistration);
		DeviceEntity deviceEntity = deviceSystemResource.register(deviceRegistration);
		log.debug("Device registration successful: '{}'", deviceEntity);

		// Process the response.
		AgentRegistrationResponse agentRegistrationResponse = new AgentRegistrationResponse();
		agentRegistrationResponse.setCertificate(deviceEntity.getDeviceKey().getCertificate());
		agentRegistrationResponse.setPublicKey(deviceEntity.getDeviceKey().getPublicKey());
		agentRegistrationResponse.setPrivateKey(deviceEntity.getDeviceKey().getPrivateKey());

		// Find the root CA to be pushed to the device.
		SettingEntity rootCa = settingsSystemResource.findByName(NamedSetting.DEVICE_ROOT_CA);
		if (rootCa == null) {
			log.warn("Root CA is not set, the device will not receive a root CA.");
		} else {
			agentRegistrationResponse.setRootCaCertificate(
				caSystemResource.getCACertificate(rootCa.asString()));
		}

		// Find the MQTT server to send back to the device.
		Optional<InfrastructureMqttEntity> mqttServer;
		if (StringUtils.isNotEmpty(agentRegistrationRequest.getTags())) {
			mqttServer = infrastructureMqttSystemResource.matchMqttServerByTags(
				agentRegistrationRequest.getTags());
		} else {
			mqttServer = infrastructureMqttSystemResource.matchRandomMqttServer();
		}
		if (mqttServer.isPresent()) {
			agentRegistrationResponse.setMqttServer(mqttServer.get().getUrl());
		} else {
			log.warn("Could not find a matching MQTT server for device '{}' with "
					+ "tags '{}' during registration.", agentRegistrationRequest.getHardwareId(),
				agentRegistrationRequest.getTags());
		}

		// Set provisioning URL.
		SettingEntity provisioningUrl = settingsSystemResource.findByName(
			NamedSetting.DEVICE_PROVISIONING_URL);
		if (provisioningUrl != null) {
			agentRegistrationResponse.setProvisioningUrl(provisioningUrl.getValue());
		} else {
			log.warn("Provisioning URL is not set.");
		}

		return agentRegistrationResponse;
	}

}
