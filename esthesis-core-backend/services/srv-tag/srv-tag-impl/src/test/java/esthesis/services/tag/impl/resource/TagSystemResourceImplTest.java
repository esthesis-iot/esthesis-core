package esthesis.services.tag.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagSystemResource;
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

@QuarkusTest
@TestHTTPEndpoint(TagSystemResource.class)
class TagSystemResourceImplTest {

	@Inject
	TagService tagService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void getAll() {

		tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/system/get-all")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_SYSTEM)
	void findByName() {
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		given()
			.accept(ContentType.JSON)
			.pathParam("name", "test-tag")
			.when().get("/v1/system/find/by-name/{name}")
			.then()
			.log().all()
			.statusCode(200)
			.contentType(ContentType.JSON);
	}
}
