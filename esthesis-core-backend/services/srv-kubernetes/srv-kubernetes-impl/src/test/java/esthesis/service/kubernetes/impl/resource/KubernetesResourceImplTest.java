package esthesis.service.kubernetes.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import esthesis.service.kubernetes.impl.service.KubernetesService;
import esthesis.service.kubernetes.resource.KubernetesResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test class for KubernetesResourceImpl, testing Kubernetes resource endpoints.
 */
@QuarkusTest
@TestHTTPEndpoint(KubernetesResource.class)
class KubernetesResourceImplTest {

	@InjectMock
	KubernetesService kubernetesService;

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void scheduleDeployment() {
		when(kubernetesService.scheduleDeployment(any())).thenReturn(true);

		DeploymentInfoDTO deploymentInfo = new DeploymentInfoDTO();


		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(deploymentInfo)
			.when().post("/v1/deployment/schedule")
			.then()
			.log().all()
			.statusCode(200)
			.body(is("true"));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getNamespaces() {
		when(kubernetesService.getNamespaces()).thenReturn(List.of("namespace1", "namespace2"));

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.when().get("/v1/namespaces")
			.then()
			.log().all()
			.statusCode(200)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void isDeploymentNameAvailable() {
		when(kubernetesService.isDeploymentNameAvailable(anyString(), anyString())).thenReturn(true);

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.queryParam("name", "test-deployment")
			.queryParam("namespace", "test-namespace")
			.when().get("/v1/deployment/check-name-available")
			.then()
			.log().all()
			.statusCode(200)
			.body(is("true"));
	}
}
