package esthesis.services.audit.impl.resource;

import static io.restassured.RestAssured.given;

import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditResource;
import esthesis.services.audit.impl.TestHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(AuditResource.class)
class AuditResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Test
	void find() {
		given()
			.when().get("/v1/find?page=0&size=10&sort=createdOn,desc")
			.then()
			.statusCode(200);
	}

	@Test
	void getCategories() {
		given()
			.when().get("/v1/categories")
			.then()
			.statusCode(200);
	}

	@Test
	void getOperations() {
	}

	@Test
	void findById() {
		AuditEntity auditEntity = testHelper.createAuditEntity();

		given()
			.when().get("/v1/" + auditEntity.getId().toHexString())
			.then()
			.statusCode(200)
			.body("id", org.hamcrest.Matchers.equalTo(auditEntity.getId().toHexString()));
	}

	@Test
	void delete() {
	}

	@Test
	void save() {
	}
}
