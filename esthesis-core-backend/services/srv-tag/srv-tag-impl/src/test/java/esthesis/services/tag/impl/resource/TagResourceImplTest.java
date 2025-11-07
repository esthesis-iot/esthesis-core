package esthesis.services.tag.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.tag.impl.TestHelper;
import esthesis.services.tag.impl.service.TagService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Test class for TagResourceImpl, testing tag resource endpoints.
 */
@QuarkusTest
@TestHTTPEndpoint(TagResource.class)
class TagResourceImplTest {

	@Inject
	TagService tagService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void find() {
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/find")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void getAll() {
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/get-all")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByName() {
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.pathParam("name", "test-tag")
			.when().get("/v1/find/by-name/{name}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findById() {
		TagEntity tag = tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.pathParam("id", tag.getId().toHexString())
			.when().get("/v1/{id}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByIds() {
		TagEntity tag = tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.queryParam("ids", tag.getId().toHexString())
			.when().get("/v1/find/by-ids")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findByNames() {
		TagEntity tag = tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.queryParam("names", tag.getName())
			.when().get("/v1/find/by-names")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {

		TagEntity tag = tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.pathParam("id", tag.getId().toHexString())
			.when().delete("/v1/{id}")
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void save() {

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(new TagEntity("test-tag", "test description"))
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}
}
