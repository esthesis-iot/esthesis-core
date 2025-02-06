package esthesis.services.device.impl.service;

import esthesis.common.util.EsthesisCommonConstants.Device.Type;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.DeviceKeyDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.service.tag.entity.TagEntity;
import esthesis.services.device.impl.repository.DeviceAttributeRepository;
import esthesis.services.device.impl.repository.DeviceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.bson.types.ObjectId;
import org.instancio.Instancio;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

@ApplicationScoped
public class TestHelper {

	@Inject
	DeviceRepository deviceRepository;

	@Inject
	DeviceAttributeRepository deviceAttributeRepository;

	Map<String, String> tagsIdMap = new HashMap<>();

	public void setup() {
		deviceRepository.deleteAll();
		deviceAttributeRepository.deleteAll();
		tagsIdMap = new HashMap<>();
		prepareTags();
	}


	/**
	 * Prepare a list of tags to be used in the return of the mocked tag resources requests.
	 */
	private void prepareTags() {
		tagsIdMap = new HashMap<>();
		tagsIdMap.put("tag1", new ObjectId().toHexString());
		tagsIdMap.put("tag2", new ObjectId().toHexString());
	}


	public DeviceEntity makeDeviceEntity(String hardwareId,
																			 Status status,
																			 String tags,
																			 Type type) {
		return Instancio.of(DeviceEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(field(DeviceEntity.class, "hardwareId"), hardwareId)
			.set(field(DeviceEntity.class, "status"), status)
			.set(field(DeviceEntity.class, "tags"), Arrays.stream(tags.split(",")).toList())
			.set(field(DeviceEntity.class, "type"), type)
			.set(field(DeviceEntity.class, "createdOn"), Instant.now().minus(1, ChronoUnit.DAYS))
			.set(field(DeviceEntity.class, "registeredOn"), Instant.now().minus(12, ChronoUnit.HOURS))
			.set(field(DeviceEntity.class, "lastSeen"), Instant.now().minus(5, ChronoUnit.MINUTES))
			.set(field(DeviceEntity.class, "deviceKey"), makeDeviceKey())
			.create();

	}

	private DeviceKeyDTO makeDeviceKey() {
		return new DeviceKeyDTO()
			.setPrivateKey("test-private-key")
			.setPublicKey("test-public-key")
			.setCertificate("test-certificate")
			.setCertificateCaId("test-ca-id");
	}


	public TagEntity makeTag(String tagName) {
		return Instancio.of(TagEntity.class)
			.set(field(BaseEntity.class, "id"), new ObjectId(tagsIdMap.get(tagName)))
			.set(field(TagEntity.class, "name"), tagName)
			.create();
	}


	public List<DevicePageFieldEntity> getDevicePageFields() {
		return List.of(
			new DevicePageFieldEntity().setMeasurement("test-measurement-1").setLabel("test-label-1").setShown(true).setFormatter("${val}").setIcon("test-icon-1"),
			new DevicePageFieldEntity().setMeasurement("test-measurement-2").setLabel("test-label-2").setShown(true).setFormatter("${val}%").setIcon("test-icon-2"),
			new DevicePageFieldEntity().setMeasurement("test-measurement-3").setLabel("test-label-3").setShown(false).setFormatter("${val}").setIcon("test-icon-3")
		);
	}

	public List<Triple<String, String, Instant>> mockRedisHashTriplets(String key, String value) {
		List<Triple<String, String, Instant>> triplets = new ArrayList<>();
		triplets.add(new ImmutableTriple<>(key, value, Instant.now()));
		return triplets;
	}

	public BufferedReader getBufferedReaderForImportData() {
		String testElp = """
			test-category test-measurement-1=10.1f 2025-01-01T00:00:00.000Z
			test-category test-measurement-2=10.2f 2025-01-01T00:00:00.000Z
			test-category test-measurement-3=10.3f 2025-01-01T00:00:00.000Z
			test-category test-measurement-4=10.4f 2025-01-01T00:00:00.000Z
			""";
		return new BufferedReader(
			new StringReader(testElp)
		);
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

	public String getTagId(String tagName) {
		return tagsIdMap.get(tagName);
	}

}
