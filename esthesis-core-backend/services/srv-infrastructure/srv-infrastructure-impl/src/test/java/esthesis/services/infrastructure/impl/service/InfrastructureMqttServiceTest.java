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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

	int initialEntitiesInDB = 0;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		testHelper.createEntities();

		when(tagSystemResource.findByName("tag1")).thenReturn(testHelper.findTagByName("tag1"));
		when(tagSystemResource.findByName("tag2")).thenReturn(testHelper.findTagByName("tag2"));
		when(tagSystemResource.findByName("tag3")).thenReturn(testHelper.findTagByName("tag3"));

		initialEntitiesInDB = testHelper.findAllInfrastructureMqttEntity().size();

		log.info("Initial entities in DB: {}", initialEntitiesInDB);
	}


	@Test
	void matchByTags() {
		// Act
		InfrastructureMqttEntity existingMqtt = infrastructureMqttService.matchByTags("tag2").orElse(null);
		InfrastructureMqttEntity nonExistingMqttTags = infrastructureMqttService.matchByTags("unexistingTag").orElse(null);
		InfrastructureMqttEntity emptyMqttTags = infrastructureMqttService.matchByTags("").orElse(null);

		//Assert - Find the MQTT server matching the provided tags or return a random MQTT server as a fallback
		assertNotNull(existingMqtt);
		assertEquals("MQTT2", existingMqtt.getName());
		assertNotNull(nonExistingMqttTags);
		assertNotNull(emptyMqttTags);
	}

	@Test
	void matchRandom() {
		// Act
		Optional<InfrastructureMqttEntity> infrastructureMqtt = infrastructureMqttService.matchRandom();

		//Assert
		assertTrue(infrastructureMqtt.isPresent());
	}

	@Test
	void find() {
		// Act
		List<InfrastructureMqttEntity> infrastructureMqttEntities =
			infrastructureMqttService.find(testHelper.makePageable(0, 100), true).getContent();

		//Assert
		assertEquals(initialEntitiesInDB, infrastructureMqttEntities.size());

	}


	@Test
	void findById() {
		// Arrange
		String validId = testHelper.findOneInfrastructureMqttEntity().getId().toString();
		String nonExistingId = new ObjectId().toString();

		// Act
		InfrastructureMqttEntity existingMqtt = infrastructureMqttService.findById(validId);
		InfrastructureMqttEntity nonExistingMqtt = infrastructureMqttService.findById(nonExistingId);

		// Assert
		assertNull(nonExistingMqtt);
		assertNotNull(existingMqtt);
	}

	@Test
	void saveNew() {
		// Arrange
		InfrastructureMqttEntity newInfrastructureMqttEntity = new InfrastructureMqttEntity();
		newInfrastructureMqttEntity.setName("test-name");
		newInfrastructureMqttEntity.setTags(List.of("tag1", "tag2"));
		newInfrastructureMqttEntity.setUrl("http://localhost.test");
		newInfrastructureMqttEntity.setActive(true);

		// Act
		InfrastructureMqttEntity savedInfrastructureMqttEntity = infrastructureMqttService.saveNew(newInfrastructureMqttEntity);

		// Assert
		assertNotNull(savedInfrastructureMqttEntity);
		assertEquals(initialEntitiesInDB + 1, testHelper.findAllInfrastructureMqttEntity().size());
	}

	@Test
	void saveUpdate() {
		// Arrange
		InfrastructureMqttEntity existingInfrastructureMqttEntity = testHelper.findOneInfrastructureMqttEntity();
		existingInfrastructureMqttEntity.setName("updated-name");
		existingInfrastructureMqttEntity.setTags(List.of("tag1", "tag2", "tag3"));
		existingInfrastructureMqttEntity.setUrl("http://localhost.test.updated");
		existingInfrastructureMqttEntity.setActive(false);

		// Act
		infrastructureMqttService.saveUpdate(existingInfrastructureMqttEntity);

		InfrastructureMqttEntity updatedInfrastructureMqttEntity =
			testHelper.findInfrastructureMqttEntityById(existingInfrastructureMqttEntity.getId().toString());

		// Assert
		assertEquals(initialEntitiesInDB, testHelper.findAllInfrastructureMqttEntity().size());
		assertEquals("updated-name", updatedInfrastructureMqttEntity.getName());
		assertEquals(3, updatedInfrastructureMqttEntity.getTags().size());
		assertEquals("http://localhost.test.updated", updatedInfrastructureMqttEntity.getUrl());
		assertFalse(updatedInfrastructureMqttEntity.isActive());
	}

	@Test
	void deleteById() {
		// Arrange
		String existingId = testHelper.findOneInfrastructureMqttEntity().getId().toString();
		String nonExistingId = new ObjectId().toString();

		// Act
		infrastructureMqttService.deleteById(nonExistingId);
		infrastructureMqttService.deleteById(existingId);

		// Assert
		assertEquals(initialEntitiesInDB - 1, testHelper.findAllInfrastructureMqttEntity().size());
	}
}
