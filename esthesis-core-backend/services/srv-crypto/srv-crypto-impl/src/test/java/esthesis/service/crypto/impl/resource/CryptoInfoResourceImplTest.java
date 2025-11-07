package esthesis.service.crypto.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.crypto.resource.CryptoInfoResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

/**
 * Test class for CryptoInfoResourceImpl, testing crypto information resource endpoints.
 */
@QuarkusTest
@TestHTTPEndpoint(CryptoInfoResource.class)
class CryptoInfoResourceImplTest {

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getSupportedKeystoreTypes() {
		given()
			.contentType(ContentType.JSON)
			.when().get("/v1/keystore-types")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("$", not(empty()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getSupportedKeyAlgorithms() {
		given()
			.contentType(ContentType.JSON)
			.when().get("/v1/key-algorithms")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("$", not(empty()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getSupportedSignatureAlgorithms() {
		given()
			.contentType(ContentType.JSON)
			.when().get("/v1/signature-algorithms")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("$", not(empty()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getSupportedMessageDigestAlgorithms() {
		given()
			.contentType(ContentType.JSON)
			.when().get("/v1/message-digest-algorithms")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("$", not(empty()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getSupportedKeyAgreementAlgorithms() {
		given()
			.contentType(ContentType.JSON)
			.when().get("/v1/key-agreement-algorithms")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("$", not(empty()));
	}
}
