package esthesis.service.agent.impl.service;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QLimitException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QSecurityException;
import esthesis.core.common.AppConstants;
import esthesis.core.common.dto.SignatureVerificationRequestDTO;
import esthesis.service.agent.dto.AgentProvisioningInfoResponse;
import esthesis.service.common.gridfs.GridFSDTO;
import esthesis.service.common.gridfs.GridFSService;
import esthesis.service.crypto.resource.CASystemResource;
import esthesis.service.crypto.resource.SigningSystemResource;
import esthesis.service.device.dto.DeviceKeyDTO;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.infrastructure.resource.InfrastructureMqttSystemResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.resource.ProvisioningSystemResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsSystemResource;
import esthesis.util.redis.RedisUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static esthesis.common.util.EsthesisCommonConstants.Device.Capability.EXECUTE;
import static esthesis.common.util.EsthesisCommonConstants.Device.Capability.PROVISIONING;
import static esthesis.common.util.EsthesisCommonConstants.Device.Type.CORE;
import static esthesis.core.common.AppConstants.GridFS.PROVISIONING_BUCKET_NAME;
import static esthesis.core.common.AppConstants.GridFS.PROVISIONING_METADATA_NAME;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_PROVISIONING_CACHE_TIME;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_PROVISIONING_SECURE;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_KEY_ALGORITHM;
import static esthesis.core.common.AppConstants.NamedSetting.SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class AgentServiceTest {

	@Inject
	AgentService agentService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	CASystemResource caSystemResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsSystemResource settingsSystemResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	InfrastructureMqttSystemResource infrastructureMqttSystemResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	DeviceSystemResource deviceSystemResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	ProvisioningSystemResource provisioningAgentResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SigningSystemResource signingSystemResource;

	@Inject
	RedisUtils redisUtils;

	@InjectMock
	GridFSService gridFSService;


	/**
	 * Helper method to create a new provisioning package instance.
	 *
	 * @param name      The name of the provisioning package
	 * @param version   The version of the provisioning package
	 * @param available The availability of the provisioning package
	 * @return a new instance of a ProvisioningPackageEntity
	 */
	private ProvisioningPackageEntity newProvisioningPackage(String name, String version, boolean available) {
		ProvisioningPackageEntity provisioningPackage =
			ProvisioningPackageEntity.builder()
				.name(name)
				.version(version)
				.available(available)
				.build();
		provisioningPackage.setId(new ObjectId());

		return provisioningPackage;
	}

	@SneakyThrows
	@BeforeEach
	void setupMocks() {
		// Mock CA certificate retrieval
		when(caSystemResource.getCACertificate("test-ca-id")).thenReturn("test-ca");

		// Mocking security-related settings
		when(settingsSystemResource.findByName(DEVICE_PROVISIONING_SECURE))
			.thenReturn(new SettingEntity().setValue("true"));
		when(settingsSystemResource.findByName(SECURITY_ASYMMETRIC_KEY_ALGORITHM))
			.thenReturn(new SettingEntity().setValue("test-algorithm"));
		when(settingsSystemResource.findByName(SECURITY_ASYMMETRIC_SIGNATURE_ALGORITHM))
			.thenReturn(new SettingEntity().setValue("test-signature-algorithm"));

		// Mocking public key retrieval
		when(deviceSystemResource.findPublicKey("test-hardware"))
			.thenReturn("test-public-key");
		when(deviceSystemResource.findPublicKey("test-hardware-2"))
			.thenReturn("test-public-key");
		when(deviceSystemResource.findPublicKey("test-hardware-3"))
			.thenReturn(null);
		when(deviceSystemResource.findPublicKey("test-hardware-4"))
			.thenReturn("test-public-key");

		// Mocking signature verification to return true
		when(signingSystemResource.verifySignature(any(SignatureVerificationRequestDTO.class)))
			.thenReturn(true);

		// Mocking provisioning package from provisioning agent resource
		when(provisioningAgentResource.findById("test-package-id"))
			.thenReturn(newProvisioningPackage("test-provisioning-package", "v0.0.1", true));
		when(provisioningAgentResource.find("test-hardware", "v0.0.1"))
			.thenReturn(newProvisioningPackage("test-provisioning-package", "v0.0.1", true));

		// Mocking provisioning package cache time
		when(settingsSystemResource.findByName(DEVICE_PROVISIONING_CACHE_TIME))
			.thenReturn(new SettingEntity().setValue("10"));

		// Simulate provisioning limit reached scenario
		int requestsPerTimeslot = 5;
		for (int i = 0; i < requestsPerTimeslot; i++) {
			redisUtils.incrCounter(RedisUtils.KeyType.ESTHESIS_PRT, "test-hardware-4",
				300);
		}

		// Mock provisioning package file retrieval from GridFS
		redisUtils.setToHash(RedisUtils.KeyType.ESTHESIS_PPDT,
			"test-download-token",
			AppConstants.Provisioning.Redis.DOWNLOAD_TOKEN_PACKAGE_ID,
			"test-file-id");

		when(gridFSService.downloadBinary(GridFSDTO.builder()
			.database("esthesiscore-test") // set in application-test.yaml
			.metadataName(PROVISIONING_METADATA_NAME)
			.metadataValue("test-file-id")
			.bucketName(PROVISIONING_BUCKET_NAME)
			.build()))
			.thenReturn(Uni.createFrom().item("test-file-content".getBytes(StandardCharsets.UTF_8)));

		// Mocking device registration
		when(deviceSystemResource.register(any(DeviceRegistrationDTO.class)))
			.thenReturn(new DeviceEntity()
				.setDeviceKey(
					new DeviceKeyDTO()
						.setPrivateKey("test-private-key")
						.setPublicKey("test-public-key")
						.setCertificate("test-certificate")
				)
				.setHardwareId("test-hardware")
			);
	}

	@BeforeEach
	void resetCounters() {
		// Reset provisioning request counter for hardware "test-hardware-4" after each test
		redisUtils.resetCounter(RedisUtils.KeyType.ESTHESIS_PRT, "test-hardware-4");
	}

	@Test
	void testFindProvisioningPackageByIdOK() {
		// Test provisioning package retrieval when available
		AgentProvisioningInfoResponse infoResponse =
			agentService.findProvisioningPackageById("test-hardware",
				"test-package-id", Optional.of("test-token"));

		assertNotNull(infoResponse);
		assertEquals("test-provisioning-package", infoResponse.getName());

		// Test when no provisioning package is available
		AgentProvisioningInfoResponse infoResponse2 =
			agentService.findProvisioningPackageById("test-hardware-2",
				"test-package-id-2", Optional.of("test-token-2"));

		assertNotNull(infoResponse2);
		assertNull(infoResponse2.getName());
	}

	@Test
	void testFindProvisioningPackageByIdNOK() {

		// Test when security settings are missing
		assertThrows(QSecurityException.class, () ->
			agentService.findProvisioningPackageById("test-hardware-3",
				"test-package-id3", Optional.of("test-token")));

		// Test when request limit is reached
		assertThrows(QLimitException.class, () ->
			agentService.findProvisioningPackageById("test-hardware-4",
				"test-package-id4", Optional.of("test-token")));

		// Test when security token is missing
		assertThrows(QSecurityException.class, () ->
			agentService.findProvisioningPackageById("test-hardware-5",
				"test-package-id5", Optional.empty()));

	}

	@Test
	void findProvisioningPackageOK() {
		// Test when a new provisioning package is available
		AgentProvisioningInfoResponse infoResponse =
			agentService.findProvisioningPackage("test-hardware", "v0.0.1", Optional.of("test-token"));

		assertNotNull(infoResponse);
		assertEquals("test-provisioning-package", infoResponse.getName());

		// Test when none provisioning package is available
		AgentProvisioningInfoResponse infoResponse2 =
			agentService.findProvisioningPackage("test-hardware-2", "v0.0.1", Optional.of("test-token"));

		assertNotNull(infoResponse2);
		assertNull(infoResponse2.getName());
	}

	@Test
	void findProvisioningPackageNOK() {
		// Test when security settings are missing
		assertThrows(QSecurityException.class, () ->
			agentService.findProvisioningPackage("test-hardware-3", "v0.0.1", Optional.of("test-token")));

		// Test when the request limit is reached
		assertThrows(QLimitException.class, () ->
			agentService.findProvisioningPackage("test-hardware-4", "v0.0.1", Optional.of("test-token")));

		// Test when security token is missing
		assertThrows(QSecurityException.class, () ->
			agentService.findProvisioningPackage("test-hardware", "v0.0.1", Optional.empty()));
	}

	@Test
	void downloadProvisioningPackageOK() {
		byte[] binary = agentService.downloadProvisioningPackage("test-download-token")
			.await().indefinitely();  // Block until the Uni completes

		String value = new String(binary, StandardCharsets.UTF_8);
		assertEquals("test-file-content", value);
	}

	@Test
	void downloadProvisioningPackageNOK() {
		assertThrows(QDoesNotExistException.class,
			() -> agentService.downloadProvisioningPackage("test-download-token-nonexistent").await().indefinitely());
	}

	@SneakyThrows
	@Test
	void registerOK() {
		AgentRegistrationRequest testAgentRequest1 =
			AgentRegistrationRequest.builder()
				.hardwareId("test-hardware_123")
				.tags("test-tag-1,test-tag-2")
				.type(CORE)
				.attributes("test-attribute-1, test-attribute-2")
				.registrationSecret("test-registration-secret")
				.capabilities(List.of(EXECUTE, PROVISIONING))
				.build();

		AgentRegistrationResponse testAgentResponse1 = agentService.register(testAgentRequest1);

		assertNotNull(testAgentResponse1);
		assertEquals(testAgentResponse1.getCertificate(), "test-certificate");
		assertEquals(testAgentResponse1.getPublicKey(), "test-public-key");
		assertEquals(testAgentResponse1.getPrivateKey(), "test-private-key");
	}

	@SneakyThrows
	@Test
	void registerNOK() {

		// Test when Hardware id has special characters
		AgentRegistrationRequest testAgentRequestWithInvalidName1 =
			AgentRegistrationRequest.builder()
				.hardwareId("test-hardware@#$")
				.build();

		assertThrows(QMismatchException.class, () -> agentService.register(testAgentRequestWithInvalidName1));

		//Test when Hardware id has spaces
		AgentRegistrationRequest testAgentRequestWithInvalidName2 =
			AgentRegistrationRequest.builder()
				.hardwareId("test hardware")
				.build();

		assertThrows(QMismatchException.class, () -> agentService.register(testAgentRequestWithInvalidName2));
	}
}
