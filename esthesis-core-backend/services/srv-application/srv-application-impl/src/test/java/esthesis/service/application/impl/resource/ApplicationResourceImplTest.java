package esthesis.service.application.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.application.impl.service.ApplicationService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(ApplicationResourceImpl.class)
class ApplicationResourceImplTest {

	@InjectMock
	ApplicationService applicationService;

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void testFindApplications() {
		// Prepare the mock data for returning a valid application.
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setCreatedOn(Instant.now());
		applicationEntity.setId(new ObjectId());
		applicationEntity.setName("Test App");
		applicationEntity.setState(Boolean.TRUE);
		Page<ApplicationEntity> mockPage = new Page<>();
		mockPage.setContent(List.of(applicationEntity));

		when(applicationService.find(any(Pageable.class))).thenReturn(mockPage);

		// Perform the request and check the response is OK.
		given()
			.queryParam("page", 0)
			.queryParam("size", 10)
			.when()
			.get("/v1/find")
			.then()
			.statusCode(OK.getStatusCode())
			.body("content.size()", is(1))
			.body("content[0].id", notNullValue())
			.body("content[0].name", is("Test App"));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void testFindApplicationById() {
		// Prepare the mock data for returning a valid application.
		ApplicationEntity applicationEntity = new ApplicationEntity();
		applicationEntity.setCreatedOn(Instant.now());
		applicationEntity.setId(new ObjectId());
		applicationEntity.setName("Test App");
		applicationEntity.setState(Boolean.TRUE);

		when(applicationService.findById(anyString())).thenReturn(applicationEntity);

		// Perform the request and check the response is OK.
		given()
			.pathParam("id", applicationEntity.getId().toHexString())
			.when()
			.get("/v1/{id}")
			.then()
			.statusCode(OK.getStatusCode())
			.body("id", is(applicationEntity.getId().toHexString()))
			.body("name", is("Test App"));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void testDelete() {
		when(applicationService.deleteById(anyString())).thenReturn(Boolean.TRUE);

		// Perform the request and check the response is OK.
		given()
			.pathParam("id", new ObjectId().toHexString())
			.when()
			.delete("/v1/{id}")
			.then()
			.statusCode(OK.getStatusCode());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void testSave() {

		// Arrange a new application.
		ApplicationEntity newApplication = new ApplicationEntity();
		newApplication.setName("Test App");
		newApplication.setToken("test-token");
		newApplication.setState(Boolean.TRUE);


		// Perform and check the response for saving a new application.
		given()
			.contentType(ContentType.JSON)
			.body(newApplication)
			.when()
			.post("/v1")
			.then()
			.log().all()
			.statusCode(NO_CONTENT.getStatusCode());

	}

}
