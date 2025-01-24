package esthesis.service.provisioning.impl;

import esthesis.core.common.AppConstants.Provisioning;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.instancio.Instancio;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.server.core.multipart.DefaultFileUpload;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.mockito.Mockito;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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


	public DeviceEntity createDeviceEntity(String hardwareId, List<String> tags) {
		return Instancio.of(DeviceEntity.class)
			.set(field(BaseEntity.class, "id"), new ObjectId())
			.set(field(DeviceEntity.class, "hardwareId"), hardwareId)
			.set(field(DeviceEntity.class, "tags"), tags)
			.set(field(DeviceEntity.class, "createdOn"), Instant.now().minus(1, ChronoUnit.DAYS))
			.set(field(DeviceEntity.class, "registeredOn"), Instant.now().minus(1, ChronoUnit.DAYS))
			.set(field(DeviceEntity.class, "lastSeen"), Instant.now().minus(1, ChronoUnit.MINUTES))
			.create();
	}

	public ProvisioningPackageEntity createProvisioningPackageEntity(String name,
																																	 String version,
																																	 String prerequisiteVersion,
																																	 boolean available,
																																	 List<String> tags,
																																	 Provisioning.Type type) {
		return Instancio.of(ProvisioningPackageEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(field(ProvisioningPackageEntity.class, "version"), version)
			.set(field(ProvisioningPackageEntity.class, "prerequisiteVersion"), prerequisiteVersion)
			.set(field(ProvisioningPackageEntity.class, "tags"), tags)
			.set(field(ProvisioningPackageEntity.class, "name"), name)
			.set(field(ProvisioningPackageEntity.class, "type"), type)
			.set(field(ProvisioningPackageEntity.class, "available"), available)
			.set(field(ProvisioningPackageEntity.class, "createdOn"), Instant.now().minus(1, ChronoUnit.DAYS))
			.create();
	}


	public void clearDatabase() {
		provisioningRepository.deleteAll();
	}

	/**
	 * Create a mocked FileUpload object with a file from the test resources' directory.
	 *
	 * @return The mocked FileUpload object.
	 */
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

	/**
	 * Mock a Pageable object with the specified parameters.
	 *
	 * @param page The page number being requested.
	 * @param size The size of the page.
	 * @return The mocked Pageable object.
	 */
	public Pageable makePageable(int page, int size) {

		// Mock the request URI and parameters.
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/find?page=" + page + "&size=" + size));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort("");
		pageable.setUriInfo(uriInfo);
		return pageable;
	}
}
