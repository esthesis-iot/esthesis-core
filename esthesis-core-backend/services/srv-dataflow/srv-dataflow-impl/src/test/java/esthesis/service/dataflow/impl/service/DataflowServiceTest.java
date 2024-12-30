package esthesis.service.dataflow.impl.service;

import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.dataflow.dto.FormlySelectOption;
import esthesis.service.dataflow.entity.DataflowEntity;
import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import esthesis.service.kubernetes.resource.KubernetesResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class DataflowServiceTest {

	@Inject
	TestHelper testHelper;

	@Inject
	DataflowService dataflowService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	KubernetesResource kubernetesResource;

	int initialDataflowSizeInDB = 0;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		testHelper.createDataflow();
		initialDataflowSizeInDB = testHelper.findAllDataflowEntity().size();

		log.info("Initial dataflow size in DB: {}", initialDataflowSizeInDB);

		// Mock the kubernetes resource requests
		when(kubernetesResource.scheduleDeployment(Mockito.any(DeploymentInfoDTO.class))).thenReturn(true);
		when(kubernetesResource.getNamespaces()).thenReturn(testHelper.getNamespaces());
		when(kubernetesResource.isDeploymentNameAvailable(anyString(), anyString())).thenReturn(true);
	}

	@Test
	void getNamespaces() {
		// Arrange
		int expectedSize = testHelper.getNamespaces().size();

		// Act
		List<FormlySelectOption> namespaces = dataflowService.getNamespaces();

		// Assert
		assertEquals(expectedSize, namespaces.size());
	}

	@Test
	void delete() {
		// Arrange
		String nonexistentDataflowId = new ObjectId().toString();
		String validDataflowId = testHelper.findOneDataflowEntity().getId().toString();

		// Act try to delete nonexistent dataflow
		assertThrows(QDoesNotExistException.class, () -> dataflowService.delete(nonexistentDataflowId));

		// Assert dataflow is not deleted
		assertEquals(initialDataflowSizeInDB, testHelper.findAllDataflowEntity().size());

		// Act try to delete valid dataflow
		dataflowService.delete(validDataflowId);

		// Assert dataflow is deleted
		assertEquals(initialDataflowSizeInDB - 1, testHelper.findAllDataflowEntity().size());
	}

	@Test
	void saveNew() {
		// Arrange
		DataflowEntity newDataflow = new DataflowEntity();
		newDataflow.setName("new dataflow");
		newDataflow.setType("new type");
		newDataflow.setStatus(true);
		newDataflow.setConfig(testHelper.createDataflowConfig());

		// Act
		DataflowEntity savedDataflow = dataflowService.saveNew(newDataflow);

		// Assert
		assertEquals(initialDataflowSizeInDB + 1, testHelper.findAllDataflowEntity().size());
		assertEquals("new dataflow", savedDataflow.getName());
	}


	@Test
	void saveUpdate() {
		// Arrange
		DataflowEntity existingDataflow = testHelper.findOneDataflowEntity();
		existingDataflow.setName("updated dataflow");
		existingDataflow.setType("updated type");
		existingDataflow.setStatus(false);

		// Act
		DataflowEntity savedDataflow = dataflowService.saveUpdate(existingDataflow);

		// Assert dataflow is updated
		assertEquals(initialDataflowSizeInDB, testHelper.findAllDataflowEntity().size());
		assertEquals(existingDataflow.getId(), savedDataflow.getId());
	}

	@Test
	void find() {
		// Act
		List<DataflowEntity> dataflowEntities =
			dataflowService.find(testHelper.makePageable(0, 100), true).getContent();

		// Assert
		assertEquals(initialDataflowSizeInDB, dataflowEntities.size());

	}

	@Test
	void findById() {
		//Arrange
		String validDataflowId = testHelper.findOneDataflowEntity().getId().toString();
		String nonexistentDataflowId = new ObjectId().toString();

		// Act & Assert
		assertNull(dataflowService.findById(nonexistentDataflowId));
		assertNotNull(dataflowService.findById(validDataflowId));

	}

	@Test
	void isDeploymentNameAvailable() {
		// Act & Assert
		assertTrue(dataflowService.isDeploymentNameAvailable("test-name", "test-namespace"));
	}
}
