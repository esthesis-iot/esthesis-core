package esthesis.service.dataflow.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.dataflow.entity.DataflowEntity;
import esthesis.service.dataflow.impl.service.DataflowService;
import esthesis.service.dataflow.impl.service.TestHelper;
import esthesis.service.dataflow.resource.DataflowResource;
import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import esthesis.service.kubernetes.resource.KubernetesResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(DataflowResource.class)
class DataflowResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	DataflowService dataflowService;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	KubernetesResource kubernetesResource;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock kubernetes resource methods.
		when(kubernetesResource.scheduleDeployment(Mockito.any(DeploymentInfoDTO.class))).thenReturn(true);
		when(kubernetesResource.getNamespaces()).thenReturn(testHelper.getNamespaces());
		when(kubernetesResource.isDeploymentNameAvailable(anyString(), anyString())).thenReturn(true);

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {
		dataflowService.saveNew(testHelper.createDataflow("test-dataflow"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/find?page=0&size=10&sort=createdOn,desc")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.isEmpty()", equalTo(false));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findById() {
		DataflowEntity dataflow = dataflowService.saveNew(testHelper.createDataflow("test-dataflow"));

		given()
			.accept(ContentType.JSON)
			.pathParam("id", dataflow.getId().toHexString())
			.when().get("/v1/{id}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("name", equalTo(dataflow.getName()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
		DataflowEntity dataflow = dataflowService.saveNew(testHelper.createDataflow("test-dataflow"));

		given()
			.accept(ContentType.JSON)
			.pathParam("id", dataflow.getId().toHexString())
			.when().delete("/v1/{id}")
			.then()
			.log().all()
			.statusCode(200);

		assertNull(dataflowService.findById(dataflow.getId().toHexString()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {
		DataflowEntity dataflow = testHelper.createDataflow("test-dataflow");

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(dataflow)
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", notNullValue())
			.body("name", equalTo(dataflow.getName()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getNamespaces() {
		given()
			.accept(ContentType.JSON)
			.when().get("/v1/namespaces")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("$.isEmpty()", equalTo(false));
	}
}
