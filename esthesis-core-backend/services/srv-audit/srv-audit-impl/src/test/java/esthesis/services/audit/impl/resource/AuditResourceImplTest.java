/*package esthesis.services.audit.impl.resource;

import static io.restassured.RestAssured.given;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditResource;
import esthesis.services.audit.impl.TestHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(AuditResource.class)
class AuditResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {
		String username = UUID.randomUUID().toString();
		for (int i = 0; i < 10; i++) {
			testHelper.persistAuditEntityForUser(username);
		}
		given()
			.contentType("application/json")
			.when().get("/v1/find?page=0&size=10&sort=createdOn,desc&createdBy=" + username)
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("content.size()", org.hamcrest.Matchers.equalTo(10));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getCategories() {
		given()
			.when().get("/v1/categories")
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("size()", org.hamcrest.Matchers.equalTo(Category.values().length));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getOperations() {
		given()
			.when().get("/v1/operations")
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("size()", org.hamcrest.Matchers.equalTo(Operation.values().length));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findById() {
		AuditEntity auditEntity = testHelper.persistAuditEntity();

		given()
			.when().get("/v1/" + auditEntity.getId().toHexString())
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("id", org.hamcrest.Matchers.equalTo(auditEntity.getId().toHexString()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
		AuditEntity auditEntity = testHelper.persistAuditEntity();

		given()
			.when().delete("/v1/" + auditEntity.getId().toHexString())
			.then()
			.statusCode(200);

		given()
			.when().get("/v1/" + auditEntity.getId().toHexString())
			.then()
			.statusCode(204);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void deleteNotFound() {
		ObjectId id = new ObjectId();
		given()
			.when().delete("/v1/" + id)
			.then()
			.statusCode(404);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {
		AuditEntity auditEntity = testHelper.makeAuditEntity();
		String username = UUID.randomUUID().toString();
		auditEntity.setCreatedBy(username);

		given()
			.contentType(ContentType.JSON)
			.body(auditEntity)
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("createdBy", org.hamcrest.Matchers.equalTo(username));
	}
}
*/