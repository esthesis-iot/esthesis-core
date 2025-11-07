package esthesis.services.audit.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.services.audit.impl.TestHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.jboss.resteasy.reactive.RestResponse.Status.OK;

/**
 * Test class for AuditSystemResourceImpl, testing audit system resource endpoints.
 */
@QuarkusTest
@TestHTTPEndpoint(AuditSystemResource.class)
class AuditSystemResourceImplTest {

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDB();
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void find() {

		testHelper.persistAuditEntity();

		given()
			.when()
			.get("/v1/system/find?entries=10")
			.then()
			.log().all()
			.body("content.size()", org.hamcrest.Matchers.equalTo(1))
			.statusCode(OK.getStatusCode());

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void countAll() {
		testHelper.persistAuditEntity();

		given()
			.when()
			.get("/v1/system/count")
			.then()
			.log().all()
			.body(org.hamcrest.Matchers.equalTo("1"))
			.statusCode(OK.getStatusCode());
	}
}
