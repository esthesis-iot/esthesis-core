package esthesis.service.agent.impl.resource;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.common.exception.QLimitException;
import esthesis.common.exception.QSecurityException;
import esthesis.service.agent.dto.AgentProvisioningInfoResponse;
import esthesis.service.agent.impl.service.AgentService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type;
import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.TOO_MANY_REQUESTS;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(AgentResourceImpl.class)
class AgentResourceImplTest {

	@InjectMock
	AgentService agentService;

	@SneakyThrows
	@Test
	void register() {
		// Prepare the request and the expected response.
		AgentRegistrationRequest request = new AgentRegistrationRequest();
		request.setHardwareId("test-hardwareId");
		request.setType(Type.CORE);
		AgentRegistrationResponse expectedResponse = new AgentRegistrationResponse();

		// Mock the service registration method.
		when(agentService.register(any(AgentRegistrationRequest.class))).thenReturn(expectedResponse);

		// Perform the request and check the response is OK.
		given()
			.contentType(ContentType.JSON)
			.body(request)
			.when().post("/v1/register")
			.then()
			.statusCode(OK.getStatusCode());
	}

	@Test
	void testFindProvisioningPackageSuccess() {
		// Prepare the request parameters.
		String hardwareId = "test-hardwareId";
		String version = "1.0";
		String token = "token123";

		// Mock the service method for finding a provisioning package.
		when(agentService.findProvisioningPackage(anyString(), anyString(), any())).thenReturn(new AgentProvisioningInfoResponse());

		// Perform the request and check the response is OK.
		given()
			.queryParam("hardwareId", hardwareId)
			.queryParam("version", version)
			.queryParam("token", token)
			.when().get("/v1/provisioning/find")
			.then()
			.statusCode(OK.getStatusCode());
	}

	@Test
	void testFindProvisioningPackageQLimitException() {

		// Mock the service method for simulating a limit exception.
		when(agentService.findProvisioningPackage(anyString(), anyString(), any()))
			.thenThrow(new QLimitException("Limit exceeded"));

		// Perform the request and confirm the response was denied due to limit exceeded.
		given()
			.queryParam("hardwareId", "test-hardwareId")
			.queryParam("version", "1.0")
			.queryParam("token", "token123")
			.when().get("/v1/provisioning/find")
			.then()
			.statusCode(TOO_MANY_REQUESTS.getStatusCode());
	}

	@Test
	void testFindProvisioningPackageQSecurityException() {
		// Mock the service method for simulating a security exception.
		when(agentService.findProvisioningPackage(anyString(), anyString(), any()))
			.thenThrow(new QSecurityException("Unauthorized"));

		// Perform the request and confirm the request was unauthorized.
		given()
			.queryParam("hardwareId", "test-hardwareId")
			.queryParam("version", "1.0")
			.queryParam("token", "token123")
			.when().get("/v1/provisioning/find")
			.then()
			.statusCode(UNAUTHORIZED.getStatusCode());
	}


	@Test
	void testFindProvisioningPackageByIdSuccess() {
		// Prepare the request parameters.
		String hardwareId = "test-hardwareId";
		String packageId = "test-packageid";
		String token = "token123";

		// Mock the service method for finding a provisioning package by id.
		when(agentService.findProvisioningPackageById(anyString(), anyString(), any()))
			.thenReturn(new AgentProvisioningInfoResponse());

		// Perform the request and check the response is OK.
		given()
			.queryParam("hardwareId", hardwareId)
			.queryParam("packageId", packageId)
			.queryParam("token", token)
			.when().get("/v1/provisioning/find/by-id")
			.then()
			.statusCode(OK.getStatusCode());
	}

	@Test
	void testFindProvisioningPackageByIdQLimitException() {
		// Mock the service method for simulating a limit exception.
		when(agentService.findProvisioningPackageById(anyString(), anyString(), any()))
			.thenThrow(new QLimitException("Limit exceeded"));

		// Perform the request and confirm the response was denied due to limit exceeded.
		given()
			.queryParam("hardwareId", "test-hardwareId")
			.queryParam("packageId", "test-packageid")
			.queryParam("token", "token123")
			.when().get("/v1/provisioning/find/by-id")
			.then()
			.statusCode(TOO_MANY_REQUESTS.getStatusCode());
	}

	@Test
	void testFindProvisioningPackageByIdQSecurityException() {
		// Mock the service method for simulating a security exception.
		when(agentService.findProvisioningPackageById(anyString(), anyString(), any()))
			.thenThrow(new QSecurityException("Unauthorized"));

		// Perform the request and confirm the request was unauthorized.
		given()
			.queryParam("hardwareId", "test-hardwareId")
			.queryParam("packageId", "test-packageid")
			.queryParam("token", "token123")
			.when().get("/v1/provisioning/find/by-id")
			.then()
			.statusCode(UNAUTHORIZED.getStatusCode());
	}

	@Test
	void testDownloadProvisioningPackageSuccess() {
		// Prepare the request parameters.
		String token = "token123";
		byte[] expectedBytes = "test data".getBytes();

		// Mock the service method for downloading a provisioning package.
		when(agentService.downloadProvisioningPackage(anyString()))
			.thenReturn(io.smallrye.mutiny.Uni.createFrom().item(expectedBytes));

		// Perform the request and check the response is OK.
		given()
			.queryParam("token", token)
			.when().get("/v1/provisioning/download")
			.then()
			.statusCode(OK.getStatusCode())
			.body(is("test data"));
	}
}
