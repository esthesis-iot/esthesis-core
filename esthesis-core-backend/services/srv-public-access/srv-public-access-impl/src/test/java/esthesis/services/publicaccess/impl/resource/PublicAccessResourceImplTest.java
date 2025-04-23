package esthesis.services.publicaccess.impl.resource;

import esthesis.service.publicaccess.resource.PublicAccessResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestHTTPEndpoint(PublicAccessResource.class)
class PublicAccessResourceImplTest {

	@Test
	void getOidcConfig() {

		given()
			.accept(ContentType.JSON)
			.when().get("/oidc-config")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body(notNullValue());
	}
}
