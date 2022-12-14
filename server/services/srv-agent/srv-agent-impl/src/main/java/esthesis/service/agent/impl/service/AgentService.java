package esthesis.service.agent.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.AppConstants.Provisioning.Redis;
import esthesis.common.exception.QLimitException;
import esthesis.common.exception.QSecurityException;
import esthesis.service.agent.dto.AgentProvisioningInfoResponse;
import esthesis.service.agent.dto.AgentRegistrationRequest;
import esthesis.service.agent.dto.AgentRegistrationResponse;
import esthesis.service.crypto.dto.SignatureVerificationRequestDTO;
import esthesis.service.crypto.resource.CASystemResource;
import esthesis.service.crypto.resource.SigningSystemResource;
import esthesis.service.dataflow.dto.MatchedMqttServerDTO;
import esthesis.service.dataflow.resource.DataflowSystemResource;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.resource.ProvisioningAgentResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsSystemResource;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class AgentService {

  @Inject
  @RestClient
  CASystemResource caSystemResource;

  @Inject
  @RestClient
  SettingsSystemResource settingsSystemResource;

  @Inject
  @RestClient
  DataflowSystemResource dataflowSystemResource;

  @Inject
  @RestClient
  DeviceSystemResource deviceSystemResource;

  @Inject
  @RestClient
  ProvisioningAgentResource provisioningAgentResource;

  @Inject
  @RestClient
  SigningSystemResource signingSystemResource;

  @Inject
  RedisUtils redisUtils;

  // The number of seconds after which the counter for provisioning requests is reset.
  private int requestCounterTimeout = 300;

  // The number of provisioning requests that can be made within the timeout period.
  private int requestsPerTimeslot = 5;

  /**
   * Registers a new device into the system.
   */
  public AgentRegistrationResponse register(
      AgentRegistrationRequest agentRegistrationRequest)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         NoSuchProviderException, OperatorCreationException {
    DeviceRegistrationDTO deviceRegistration = new DeviceRegistrationDTO();
    deviceRegistration.setIds(agentRegistrationRequest.getHardwareId());
    if (StringUtils.isNotBlank(agentRegistrationRequest.getTags())) {
      deviceRegistration.setTags(
          Arrays.stream(agentRegistrationRequest.getTags().split(","))
              .toList());
    }
    log.debug("Requesting device registration with: '{}'", deviceRegistration);
    DeviceEntity deviceEntity = deviceSystemResource.register(deviceRegistration);

    AgentRegistrationResponse agentRegistrationResponse = new AgentRegistrationResponse();
    agentRegistrationResponse.setCertificate(
        deviceEntity.getDeviceKey().getCertificate());
    agentRegistrationResponse.setPublicKey(
        deviceEntity.getDeviceKey().getPublicKey());
    agentRegistrationResponse.setPrivateKey(
        deviceEntity.getDeviceKey().getPrivateKey());

    // Find the root CA to be pushed to the device.
    ObjectId rootCaId =
        settingsSystemResource.findByName(NamedSetting.DEVICE_ROOT_CA)
            .asObjectId();
    if (rootCaId == null) {
      log.warn("Root CA is not set.");
    } else {
      agentRegistrationResponse.setRootCaCertificate(
          caSystemResource.getCACertificate(rootCaId));
    }

    // Find the MQTT server to send back to the device.
    MatchedMqttServerDTO mqttServer = dataflowSystemResource.matchMqttServerByTags(
        Arrays.asList(agentRegistrationRequest.getTags().split(",")));
    if (mqttServer != null) {
      agentRegistrationResponse.setMqttServer(mqttServer.getUrl());
    } else {
      log.warn("Could not find a matching MQTT server for device {} with "
              + "tags {} during registration.",
          agentRegistrationRequest.getHardwareId(),
          agentRegistrationRequest.getTags());
    }

    // Set provisioning URL.
    SettingEntity provisioningUrl =
        settingsSystemResource.findByName(NamedSetting.DEVICE_PROVISIONING_URL);
    if (provisioningUrl != null) {
      agentRegistrationResponse.setProvisioningUrl(provisioningUrl.getValue());
    } else {
      log.warn("Provisioning URL is not set.");
    }

    return agentRegistrationResponse;
  }

  /**
   * Attempts to validate a request token sent by a device while requesting information on available
   * provisioning packages. The token is an RSA/SHA256 digital signature of the hardware id of the
   * device requesting the information.
   *
   * @param hardwareId
   * @param token
   * @return
   */
  private boolean validateRequestToken(String hardwareId, String token) {
    log.debug("Validating find provisioning package request token '{}' for hardware id '{}'.",
        token, hardwareId);

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
    request.setPayload(hardwareId.getBytes(StandardCharsets.UTF_8));
    request.setSignature(token);
    request.setSignatureAlgorithm(signatureAlgorithm);

    // Verify the signature.
    try {
      return signingSystemResource.verifySignature(request);
    } catch (Exception e) {
      log.error("Could not validate request token.", e);
      return false;
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
   * @param hardwareId
   * @param token
   * @return
   */
  public AgentProvisioningInfoResponse findProvisioningPackage(String hardwareId,
      Optional<String> token) {
    // If the platform is configured to only allow secure provisioning requests check the
    // requestToken provided by the device.
    if (settingsSystemResource.findByName(NamedSetting.DEVICE_PROVISIONING_SECURE).asBoolean()) {
      long counter = redisUtils.incrCounter(KeyType.ESTHESIS_PRT, hardwareId,
          requestCounterTimeout);
      if (counter > requestsPerTimeslot) {
        throw new QLimitException(
            "Device with hardware id '{}' has exceeded the number of allowed provisioning "
                + "requests, '{}' requests per '{}' seconds).", hardwareId, requestsPerTimeslot,
            requestCounterTimeout);
      }

      // Attempt to validate the request token.
      if (token.isEmpty()) {
        throw new QSecurityException(
            "Requesting provisioning package information for hardware id '{}' requires a token.",
            hardwareId);
      } else {
        if (!validateRequestToken(hardwareId, token.get())) {
          throw new QSecurityException("Invalid request token '{}' for hardware id '{}'.",
              token.get(), hardwareId);
        } else {
          log.debug("Request token '{}' for hardware id '{}' is valid.",
              token.get(), hardwareId);
        }
      }

      // If the token was validated, reset the caching counter.
      redisUtils.resetCounter(KeyType.ESTHESIS_PRT, hardwareId);
    }

    // Find a candidate provisioning package.
    log.debug("Requesting provisioning info for device with hardware ID '{}'.",
        hardwareId);
    ProvisioningPackageEntity pp = provisioningAgentResource.find(hardwareId);

    // If a provisioning package was not found terminate here.
    if (pp == null) {
      log.warn("Could not find a provisioning package for device with hardware ID '{}'.",
          hardwareId);
      return null;
    }

    // If a provisioning package was found, create a download token for it.
    log.debug("Found provisioning package '{}'.", pp);
    String randomToken = UUID.randomUUID().toString().replace("-", "");
    redisUtils.setToHash(KeyType.ESTHESIS_PPDT, randomToken,
        Redis.DOWNLOAD_TOKEN_PACKAGE_ID, pp.getId().toString());
    redisUtils.setToHash(KeyType.ESTHESIS_PPDT, randomToken,
        Redis.DOWNLOAD_TOKEN_CREATED_ON, Instant.now().toString());
    redisUtils.setExpirationForHash(KeyType.ESTHESIS_PPDT, randomToken,
        settingsSystemResource.findByName(NamedSetting.DEVICE_PROVISIONING_CACHE_TIME).asLong()
            * 60);

    // Prepare the reply with the provisioning package details and the download token.
    AgentProvisioningInfoResponse apir = new AgentProvisioningInfoResponse();
    apir.setId(pp.getId().toString());
    apir.setName(pp.getName());
    apir.setVersion(pp.getVersion());
    apir.setSize(pp.getSize());
    apir.setSha256(pp.getSha256());
    apir.setDownloadUrl(
        settingsSystemResource.findByName(NamedSetting.DEVICE_PROVISIONING_URL).asString());
    apir.setDownloadToken(randomToken);

    return apir;
  }

}
