package esthesis.services.infrastructure.impl.service;

import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.tag.resource.TagSystemResource;
import esthesis.services.infrastructure.impl.TestHelper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class InfrastructureMqttServiceTest {

	@Inject
	InfrastructureMqttService infrastructureMqttService;

	@Inject
	TestHelper testHelper;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	TagSystemResource tagSystemResource;


	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}


	@Test
	void matchByTags() {
		// Mock finding a tag named tag1.
		when(tagSystemResource.findByName("tag1")).thenReturn(testHelper.findTagByName("tag1"));

		// Perform a save of a new mqtt entity with a tag named tag1.
		infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1",
				"http://localhost.test",
				true, "tag1")
		);

		// Assert existing tag matches existing MQTT Infrastructure.
		assertTrue(infrastructureMqttService.matchByTags("tag1").isPresent());

		// Assert non-existing tag  also matches existing MQTT Infrastructure (by calling the match random method).
		assertTrue(infrastructureMqttService.matchByTags("non-existing-tag").isPresent());

	}

	@Test
	void matchRandom() {
		// Perform a save of a new mqtt entity.
		infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1",
				"http://localhost.test",
				true, "tag1")
		);

		// Assert MQTT Infrastructure is found.
		assertTrue(infrastructureMqttService.matchRandom().isPresent());
	}

	@Test
	void find() {
		// Assert no MQTT Infrastructure is found.
		assertEquals(0,
			infrastructureMqttService.find(
				testHelper.makePageable(0, 100),
				true).getContent().size());

		// Perform a save of a new MQTT Infrastructure entity.
		infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1",
				"http://localhost.test",
				true, "tag1")
		);

		// Assert the persisted MQTT Infrastructure is found.
		assertEquals(1,
			infrastructureMqttService.find(
				testHelper.makePageable(0, 100),
				true).getContent().size());

		// Todo cover more cases with different parameters.

	}


	@Test
	void findById() {
		// Assert no MQTT Infrastructure is found for non-existing id.
		assertNull(infrastructureMqttService.findById(new ObjectId().toHexString()));

		// Perform a save of a new MQTT Infrastructure entity.
		String id = infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1", "http://localhost.test",
				true, "tag1")
		).getId().toHexString();

		// Assert the persisted MQTT Infrastructure is found.
		assertNotNull(infrastructureMqttService.findById(id));


	}

	@Test
	void saveNew() {
		// Perform a save of a new MQTT Infrastructure entity.
		String id = infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1", "http://localhost.test",
				true, "tag1")
		).getId().toHexString();

		// Assert entity was persisted.
		assertNotNull(infrastructureMqttService.findById(id));
	}

	@Test
	void saveUpdate() {
		// Perform a save of a new MQTT Infrastructure entity.
		String id = infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1", "http://localhost.test",
				true, "tag1")
		).getId().toHexString();

		// Retrieve the entity and perform an update.
		InfrastructureMqttEntity infrastructureMqtt = infrastructureMqttService.findById(id);
		infrastructureMqtt.setName("MQTT2");
		infrastructureMqtt.setUrl("http://localhost.test2");
		infrastructureMqtt.setActive(false);
		infrastructureMqtt.setTags(List.of("tag2", "tag3"));
		infrastructureMqttService.saveUpdate(infrastructureMqtt);

		// Assert entity was updated.
		InfrastructureMqttEntity updatedInfrastructureMqtt = infrastructureMqttService.findById(id);
		assertEquals("MQTT2", updatedInfrastructureMqtt.getName());
		assertEquals("http://localhost.test2", updatedInfrastructureMqtt.getUrl());
		assertFalse(updatedInfrastructureMqtt.isActive());
		assertEquals(2, updatedInfrastructureMqtt.getTags().size());

	}

	@Test
	void deleteById() {
		// Perform a save of a new MQTT Infrastructure entity.
		String id = infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1", "http://localhost.test",
				true, "tag1")
		).getId().toHexString();

		// Perform a delete operation.
		infrastructureMqttService.deleteById(id);

		// Assert entity was deleted.
		assertNull(infrastructureMqttService.findById(id));
	}
}
