package esthesis.services.dashboard.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.dashboard.resource.DashboardResource;
import esthesis.service.security.resource.SecurityResource;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsSystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.service.BroadcasterService;
import esthesis.services.dashboard.impl.service.DashboardService;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.jaxrs.SseEventSinkImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(DashboardResource.class)
class DashboardResourceImplTest {

	@Inject
	TestHelper testHelper;

	@Inject
	DashboardService dashboardService;

	@Inject
	BroadcasterService broadcasterService;

	@InjectMock
	SecurityIdentity securityIdentity;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	SecurityResource securityResource;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	SettingsSystemResource settingsSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;

	// Mocked current user ID.
	ObjectId userId = new ObjectId();

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();


		// Mock the security identity for getting the current user.
		when(securityIdentity.getPrincipal())
			.thenReturn(testHelper.makePrincipal("principal-user"));

		when(securityResource.findUserByUsername("principal-user"))
			.thenReturn(testHelper.makeUser("principal-user", userId));


		when(settingsSystemResource.findByName(any())).thenReturn(new SettingEntity("test", "test"));
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);
	}


	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	@Disabled("Disabled while unexpected error is investigated.")
	void refreshSub() {
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test-dashboard"));
		broadcasterService.register(dashboard.getId().toHexString(), "test-subscription-id-2", mock(SseEventSinkImpl.class));

		given()
			.accept(ContentType.JSON)
			.pathParam("subscriptionId", "test-subscription-id-2")
			.when().get("/v1/refresh-sub/{subscriptionId}")
			.then()
			.log().all()
			.statusCode(200);
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	@Disabled("Disabled while unexpected error is investigated.")
	void unsub() {
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test-dashboard"));
		broadcasterService.register(dashboard.getId().toHexString(), "test-subscription-id-3", mock(SseEventSinkImpl.class));

		given()
			.accept(ContentType.JSON)
			.pathParam("subscriptionId", "test-subscription-id-3")
			.when().delete("/v1/sub/{subscriptionId}")
			.then()
			.log().all()
			.statusCode(204);

	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findById() {
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test-dashboard"));

		given()
			.accept(ContentType.JSON)
			.pathParam("dashboardId", dashboard.getId().toHexString())
			.when().get("/v1/{dashboardId}")
			.then()
			.log().all()
			.statusCode(200)
			.body("name", equalTo(dashboard.getName()));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findAllForCurrentUser() {
		dashboardService.saveNew(testHelper.makeDashboard("test-dashboard"));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/all-for-current-user")
			.then()
			.log().all()
			.statusCode(200)
			.body("content.isEmpty()", equalTo(false));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void findShared() {
		dashboardService.saveNew(testHelper.makeDashboard("test-dashboard").setShared(true));

		given()
			.accept(ContentType.JSON)
			.when().get("/v1/shared")
			.then()
			.log().all()
			.statusCode(200)
			.body("content.isEmpty()", equalTo(false));
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	void delete() {
		DashboardEntity dashboard = dashboardService.saveNew(testHelper.makeDashboard("test-dashboard"));

		given()
			.pathParam("dashboardId", dashboard.getId().toHexString())
			.when().delete("/v1/{dashboardId}")
			.then()
			.log().all()
			.statusCode(200);

		assertTrue(dashboardService.findByIdOptional(dashboard.getId().toHexString()).isEmpty());
	}

	@Test
	@TestSecurity(user = "test-user", roles = AppConstants.ROLE_USER)
	@Disabled("Disabled while unexpected error is investigated.")
	void save() {
		DashboardEntity dashboard = testHelper.makeDashboard("test-dashboard");

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(dashboard)
			.when().post("/v1")
			.then()
			.log().all()
			.statusCode(200)
			.body(not(emptyOrNullString()));
	}
}
