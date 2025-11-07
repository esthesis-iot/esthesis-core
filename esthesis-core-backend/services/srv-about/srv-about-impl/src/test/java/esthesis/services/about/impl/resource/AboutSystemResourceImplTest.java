package esthesis.services.about.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.about.resource.AboutSystemResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Test class for AboutSystemResourceImpl, testing about system resource endpoints.
 */
@QuarkusTest
@TestHTTPEndpoint(AboutSystemResource.class)
class AboutSystemResourceImplTest {

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void testAboutOK() {
		when().get("/v1/system/general")
			.then()
			.contentType("application/json")
			.body(notNullValue(AboutGeneralDTO.class))
			.body("gitBuildTime", notNullValue())
			.body("gitCommitId", notNullValue())
			.body("gitCommitIdAbbrev", notNullValue())
			.body("gitVersion", notNullValue())
			.statusCode(200);
	}

	@Test
	void testAboutNOK() {
		when().get("/v1/system/general")
			.then()
			.statusCode(401);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void testAboutNOK2() {
		when().get("/v1/system/general")
			.then()
			.statusCode(403);
	}

}
