package esthesis.service.dataflow.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import esthesis.common.exception.QDoesNotExistException;
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

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock scheduling the deployment, getting the namespaces and checking if the deployment name is available.
		when(kubernetesResource.scheduleDeployment(Mockito.any(DeploymentInfoDTO.class))).thenReturn(
			true);
		when(kubernetesResource.getNamespaces()).thenReturn(testHelper.getNamespaces());
		when(kubernetesResource.isDeploymentNameAvailable(anyString(), anyString())).thenReturn(true);
	}

	@Test
	void getNamespaces() {
		// Assert namespaces are returned.
		assertFalse(dataflowService.getNamespaces().isEmpty());
	}

	@Test
	void delete() {
		// Perform a save operation for a new dataflow.
		String dataflowId =
			dataflowService.saveNew(testHelper.createDataflow("test dataflow"))
				.getId()
				.toHexString();

		// Assert deletion fails for nonexistent dataflow.
		String nonexistentDataflowId = new ObjectId().toHexString();
		assertThrows(QDoesNotExistException.class, () -> dataflowService.delete(nonexistentDataflowId));

		// Assert dataflow exists.
		assertNotNull(dataflowService.findById(dataflowId));

		// Perform a delete operation for the dataflow.
		dataflowService.delete(dataflowId);

		// Assert dataflow was deleted.
		assertNull(dataflowService.findById(dataflowId));
	}

	@Test
	void saveNew() {
		// Perform a save operation for a new dataflow.
		String dataflowId =
			dataflowService.saveNew(new DataflowEntity()
					.setName("new dataflow")
					.setType("new type")
					.setStatus(true)
					.setConfig(testHelper.createDataflowConfig()))
				.getId().toHexString();

		// Assert dataflow was saved with the provided values.
		DataflowEntity savedDataflow = dataflowService.findById(dataflowId);
		assertEquals("new dataflow", savedDataflow.getName());
		assertEquals("new type", savedDataflow.getType());
		assertTrue(savedDataflow.isStatus());
		assertEquals(testHelper.createDataflowConfig(), savedDataflow.getConfig());
	}


	@Test
	void saveUpdate() {
		// Perform a save operation for a new dataflow.
		String dataflowId =
			dataflowService.saveNew(new DataflowEntity()
					.setName("new dataflow")
					.setType("new type")
					.setStatus(true)
					.setConfig(testHelper.createDataflowConfig()))
				.getId().toHexString();

		// Perform an update operation for the dataflow.
		DataflowEntity dataflow = dataflowService.findById(dataflowId);
		dataflow.setName("updated dataflow");
		dataflow.setType("updated type");
		dataflow.setStatus(false);
		dataflowService.saveUpdate(dataflow);

		// Assert dataflow was updated with the provided values.
		DataflowEntity updatedDataflow = dataflowService.findById(dataflowId);
		assertEquals("updated dataflow", updatedDataflow.getName());
		assertEquals("updated type", updatedDataflow.getType());
		assertFalse(updatedDataflow.isStatus());
	}

	@Test
	void find() {
		// Assert no dataflows exist.
		assertTrue(dataflowService.find(
				testHelper.makePageable(0, 100))
			.getContent()
			.isEmpty());

		assertTrue(dataflowService.find(
				testHelper.makePageable(0, 100))
			.getContent()
			.isEmpty());

		// Perform a save operation for a new dataflow.
		dataflowService.saveNew(testHelper.createDataflow("test dataflow"));

		// Assert dataflow exists.
		assertFalse(dataflowService.find(
				testHelper.makePageable(0, 100))
			.getContent()
			.isEmpty());

		assertFalse(dataflowService.find(
				testHelper.makePageable(0, 100))
			.getContent().
			isEmpty());

	}

	@Test
	void findById() {
		// Perform a save operation for a new dataflow.
		String dataflowId =
			dataflowService.saveNew(testHelper.createDataflow("test dataflow"))
				.getId()
				.toHexString();

		// Assert dataflow can be found.
		assertNotNull(dataflowService.findById(dataflowId));

		// Assert non-existent dataflow cannot be found.
		assertNull(dataflowService.findById(new ObjectId().toHexString()));
	}

	@Test
	void isDeploymentNameAvailable() {
		// Assert deployment name is available.
		assertTrue(dataflowService.isDeploymentNameAvailable("test-name", "test-namespace"));
	}
}
