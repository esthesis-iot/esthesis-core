package esthesis.services.infrastructure.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.infrastructure.resource.InfrastructureMqttSystemResource;
import esthesis.service.tag.resource.TagSystemResource;
import esthesis.services.infrastructure.impl.TestHelper;
import esthesis.services.infrastructure.impl.service.InfrastructureMqttService;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(InfrastructureMqttSystemResource.class)
class InfrastructureMqttSystemResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	InfrastructureMqttService infrastructureMqttService;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	TagSystemResource tagSystemResource;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		when(tagSystemResource.findByName("tag1")).thenReturn(testHelper.findTagByName("tag1"));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void matchMqttServerByTags() {
		infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1",
				"http://localhost.test",
				true, "tag1")
		);

		given()
			.accept(ContentType.JSON)
			.queryParam("tags", "tag1")
			.when().get("/v1/match-by-tag")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void matchRandomMqttServer() {
		infrastructureMqttService.saveNew(
			testHelper.createInfrastructureMQtt(
				"MQTT1",
				"http://localhost.test",
				true, "tag1")
		);

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/match-random")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}
}
