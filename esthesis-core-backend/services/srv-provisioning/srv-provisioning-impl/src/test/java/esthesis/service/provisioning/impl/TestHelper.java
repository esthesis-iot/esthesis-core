package esthesis.service.provisioning.impl;

import esthesis.core.common.AppConstants.Provisioning;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.instancio.Instancio;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.server.core.multipart.DefaultFileUpload;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;
import org.jboss.resteasy.reactive.server.multipart.FormValue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ApplicationScoped
public class TestHelper {

	@Inject
	ProvisioningRepository provisioningRepository;


	public DeviceEntity makeDeviceEntity(String hardwareId) {
		return Instancio.of(DeviceEntity.class)
			.set(field(BaseEntity.class, "id"), new ObjectId())
			.set(field(DeviceEntity.class, "hardwareId"), hardwareId)
			.set(field(DeviceEntity.class, "tags"), List.of("tag1", "tag2"))
			.set(field(DeviceEntity.class, "createdOn"), Instant.now().minus(1, ChronoUnit.DAYS))
			.set(field(DeviceEntity.class, "registeredOn"), Instant.now().minus(1, ChronoUnit.DAYS))
			.set(field(DeviceEntity.class, "lastSeen"), Instant.now().minus(1, ChronoUnit.MINUTES))
			.create();
	}

	public ProvisioningPackageEntity createProvisioningPackageEntity(String name,
																																	 String version,
																																	 boolean available,
																																	 Provisioning.Type type) {
		return Instancio.of(ProvisioningPackageEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(field(ProvisioningPackageEntity.class, "version"), version)
			.set(field(ProvisioningPackageEntity.class, "tags"), List.of("tag1", "tag2"))
			.set(field(ProvisioningPackageEntity.class, "name"), name)
			.set(field(ProvisioningPackageEntity.class, "type"), type)
			.set(field(ProvisioningPackageEntity.class, "available"), available)
			.set(field(ProvisioningPackageEntity.class, "createdOn"), Instant.now().minus(1, ChronoUnit.DAYS))
			.create();
	}


	public void createProvisioningPackages() {
		ProvisioningPackageEntity ppe1 =
			createProvisioningPackageEntity(
				"Test Internal Provisioning Package 1",
				"1.0.0",
				false,
				Provisioning.Type.INTERNAL);
		provisioningRepository.persist(ppe1);


		ProvisioningPackageEntity ppe2 =
			createProvisioningPackageEntity(
				"Test Internal Provisioning Package 2",
				"1.1.0",
				true,
				Provisioning.Type.INTERNAL);
		provisioningRepository.persist(ppe2);


		ProvisioningPackageEntity ppe3 =
			createProvisioningPackageEntity(
				"Test External Provisioning Package 1",
				"1.0.0",
				false,
				Provisioning.Type.EXTERNAL);

		provisioningRepository.persist(ppe3);

		ProvisioningPackageEntity ppe4 =
			createProvisioningPackageEntity(
				"Test External Provisioning Package 2",
				"1.1.0",
				true,
				Provisioning.Type.EXTERNAL);

		provisioningRepository.persist(ppe4);


	}


	public void clearDatabase() {
		provisioningRepository.deleteAll();
	}

	public List<ProvisioningPackageEntity> findAllProvisioningPackages() {
		return provisioningRepository.findAll().list();
	}

	public ProvisioningPackageEntity createInternalProvisioningPackage(String name, String version) {
		ProvisioningPackageEntity provisioningPackage = new ProvisioningPackageEntity();
		provisioningPackage.setName(name);
		provisioningPackage.setTags(List.of("tag1"));
		provisioningPackage.setType(Provisioning.Type.INTERNAL);
		provisioningPackage.setAttributes("attribute-1,attribute-2");
		provisioningPackage.setDescription("test description");
		provisioningPackage.setPrerequisiteVersion("1.0.0");
		provisioningPackage.setSha256("9f86d081884c7d659a2feaa0c55ad023787d6a0b123d2e5a8d70fbbd7a8a7f6e");
		provisioningPackage.setAvailable(true);
		provisioningPackage.setUrl("https://127.0.0.1/test-file.sh");
		provisioningPackage.setVersion(version);
		return provisioningPackage;
	}

	public ProvisioningPackageEntity findProvisioningPackage(String id) {
		return provisioningRepository.findById(new ObjectId(id));
	}

	@SneakyThrows
	public FileUpload createFileUpload() {

		String filename = "provisioning-test-file.bin";

		Path testPath =
			Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toURI());

		FormValue formValue = mock(FormValue.class);
		when(formValue.getFileItem()).thenReturn(new FormData.FileItemImpl(testPath));
		when(formValue.getFileName()).thenReturn(filename);


		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.add("Content-Type", "application/octet-stream");

		when(formValue.getHeaders()).thenReturn(headers);

		return new DefaultFileUpload(filename, formValue);
	}

	public ProvisioningPackageEntity findOneProvisioningPackage(Provisioning.Type type) {
		return provisioningRepository.findAll().stream()
			.filter(ppe -> ppe.getType().equals(type))
			.findFirst()
			.orElseThrow();
	}

	public List<ProvisioningPackageEntity> findAllInternalProvisioningPackages() {
		return this.findAllProvisioningPackages()
			.stream()
			.filter(ppe -> ppe.getType().equals(Provisioning.Type.INTERNAL))
			.toList();
	}

	public List<ProvisioningPackageEntity> findAllExternalProvisioningPackages() {
		return this.findAllProvisioningPackages()
			.stream()
			.filter(ppe -> ppe.getType().equals(Provisioning.Type.EXTERNAL))
			.toList();
	}
}
