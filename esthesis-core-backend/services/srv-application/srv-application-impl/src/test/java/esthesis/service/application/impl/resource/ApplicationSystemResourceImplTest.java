package esthesis.service.application.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.application.impl.service.ApplicationService;
import esthesis.service.application.resource.ApplicationSystemResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(ApplicationSystemResource.class)
class ApplicationSystemResourceImplTest {

	@InjectMock
	ApplicationService applicationService;

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void isTokenValid() {
		when(applicationService.isTokenValid("token")).thenReturn(true);

		given()
			.queryParam("token", "token")
			.when()
			.get("/v1/system/is-token-valid")
			.then()
			.statusCode(OK.getStatusCode())
			.body(is("true"));

	}
}
