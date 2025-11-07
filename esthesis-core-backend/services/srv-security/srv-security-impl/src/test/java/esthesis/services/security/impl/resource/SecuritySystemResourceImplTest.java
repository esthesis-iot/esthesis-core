package esthesis.services.security.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.security.entity.GroupEntity;
import esthesis.service.security.entity.UserEntity;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.security.impl.TestHelper;
import esthesis.services.security.impl.service.SecurityGroupService;
import esthesis.services.security.impl.service.SecurityUserService;
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

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Test class for SecuritySystemResourceImpl, testing security system resource endpoints.
 */
@QuarkusTest
@TestHTTPEndpoint(SecuritySystemResource.class)
class SecuritySystemResourceImplTest {

	@Inject
	SecurityUserService securityUserService;

	@Inject
	SecurityGroupService securityGroupService;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	AuditSystemResource auditSystemResource;


	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
		Mockito.when(auditSystemResource.countAll()).thenReturn(0L);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void isPermitted() {
		GroupEntity group = securityGroupService.saveNew(testHelper.makeGroupEntity("test-group"));
		UserEntity user = securityUserService.saveNew(testHelper.makeUserEntity(
			"test-user",
			List.of(),
			List.of(group.getId().toHexString())));

		given()
			.accept(ContentType.JSON)
			.queryParam("category", AppConstants.Security.Category.GROUPS)
			.queryParam("operation", AppConstants.Security.Operation.READ)
			.queryParam("userId", user.getId().toHexString())
			.queryParam("resourceId", "test-resource")
			.when().get("/v1/is-permitted")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void stats() {
		given()
			.accept(ContentType.JSON)
			.when().get("/v1/stats")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}
}
