package esthesis.service.provisioning.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import java.util.Arrays;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProvisioningAgentServiceTest {

	@InjectMocks
	private ProvisioningAgentService provisioningAgentService;

	@Mock
	private DeviceSystemResource deviceSystemResource;

	@Mock
	private ProvisioningRepository provisioningRepository;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testFind() {
		String hardwareId = "testHardwareId";

		String deviceVersion;
		String expectedVersion;
		DeviceEntity deviceEntity;
		List<ProvisioningPackageEntity> packages;
		ProvisioningPackageEntity result;

		// 1.0.0 -> 1.0.2, tag exists.
		deviceVersion = "1.0.0";
		expectedVersion = "1.0.2";
		deviceEntity = new DeviceEntity();
		deviceEntity.setId(new ObjectId());
		deviceEntity.setTags(List.of("tag1"));
		when(deviceSystemResource.findByHardwareId(hardwareId)).thenReturn(deviceEntity);
		packages = Arrays.asList(
			ProvisioningPackageEntity.builder().version("1.0.1").tags(List.of("tag1")).build(),
			ProvisioningPackageEntity.builder().version("1.0.2").tags(List.of("tag1")).build()
		);
		when(provisioningRepository.findByTagIds(deviceEntity.getTags())).thenReturn(packages);
		result = provisioningAgentService.find(hardwareId, deviceVersion);
		assertEquals(expectedVersion, result.getVersion());

		// 1.0.0 -> 1.0.2, tag does not exist.
		deviceVersion = "1.0.0";
		expectedVersion = "1.0.2";
		deviceEntity = new DeviceEntity();
		deviceEntity.setId(new ObjectId());
		deviceEntity.setTags(List.of("tagXYZ"));
		when(deviceSystemResource.findByHardwareId(hardwareId)).thenReturn(deviceEntity);
		packages = Arrays.asList(
			ProvisioningPackageEntity.builder().version("1.0.1").tags(List.of("tag1")).build(),
			ProvisioningPackageEntity.builder().version("1.0.2").tags(List.of("tag1")).build()
		);
		when(provisioningRepository.findByTagIds(deviceEntity.getTags())).thenReturn(packages);
		result = provisioningAgentService.find(hardwareId, deviceVersion);
		assertEquals(expectedVersion, result.getVersion());

		// 1.0.0 -> 1.0.5, multiple tags.
		deviceVersion = "1.0.0";
		expectedVersion = "1.0.5";
		deviceEntity = new DeviceEntity();
		deviceEntity.setId(new ObjectId());
		deviceEntity.setTags(List.of("tag1,tag2"));
		when(deviceSystemResource.findByHardwareId(hardwareId)).thenReturn(deviceEntity);
		packages = Arrays.asList(
			ProvisioningPackageEntity.builder().version("1.0.1").tags(List.of("tag1")).build(),
			ProvisioningPackageEntity.builder().version("1.0.2").tags(List.of("tag1")).build(),
			ProvisioningPackageEntity.builder().version("1.0.4").tags(List.of("tag2")).build(),
			ProvisioningPackageEntity.builder().version("1.0.5").tags(List.of("tag2")).build()
		);
		when(provisioningRepository.findByTagIds(deviceEntity.getTags())).thenReturn(packages);
		result = provisioningAgentService.find(hardwareId, deviceVersion);
		assertEquals(expectedVersion, result.getVersion());

		// 1.0.0 -> 2.0.0, higher versions exist in 1.0.x branch, tag exists.
		deviceVersion = "1.0.0";
		expectedVersion = "2.0.0";
		deviceEntity = new DeviceEntity();
		deviceEntity.setId(new ObjectId());
		deviceEntity.setTags(List.of("tag1"));
		when(deviceSystemResource.findByHardwareId(hardwareId)).thenReturn(deviceEntity);
		packages = Arrays.asList(
			ProvisioningPackageEntity.builder().version("1.0.1").tags(List.of("tag1")).build(),
			ProvisioningPackageEntity.builder().version("1.0.2").tags(List.of("tag1")).build(),
			ProvisioningPackageEntity.builder().version("2.0.0").tags(List.of("tag1")).build()
		);
		when(provisioningRepository.findByTagIds(deviceEntity.getTags())).thenReturn(packages);
		result = provisioningAgentService.find(hardwareId, deviceVersion);
		assertEquals(expectedVersion, result.getVersion());

		// 1.0.0 -> 1.0.9, 2.0.0 exists but has 1.0.9 as a prerequisite, tag exists.
		deviceVersion = "1.0.0";
		expectedVersion = "1.0.9";
		deviceEntity = new DeviceEntity();
		deviceEntity.setId(new ObjectId());
		deviceEntity.setTags(List.of("tag1"));
		when(deviceSystemResource.findByHardwareId(hardwareId)).thenReturn(deviceEntity);
		packages = Arrays.asList(
			ProvisioningPackageEntity.builder().version("1.0.1").tags(List.of("tag1")).build(),
			ProvisioningPackageEntity.builder().version("1.0.9").tags(List.of("tag1")).build(),
			ProvisioningPackageEntity.builder().version("2.0.0")
				.tags(List.of("tag1")).prerequisiteVersion("1.0.9").build()
		);
		when(provisioningRepository.findByTagIds(deviceEntity.getTags())).thenReturn(packages);
		result = provisioningAgentService.find(hardwareId, deviceVersion);
		assertEquals(expectedVersion, result.getVersion());
	}

}

