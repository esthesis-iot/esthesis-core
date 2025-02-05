package esthesis.services.dashboard.impl.service;

import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.resource.SecurityResource;
import esthesis.services.dashboard.impl.TestHelper;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class DashboardServiceTest {

	@Inject
	DashboardService dashboardService;

	@Inject
	TestHelper testHelper;

	@InjectMock
	SecurityIdentity securityIdentity;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SecurityResource securityResource;

	// Mocked user ID.
	ObjectId userId = new ObjectId();

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Mock the security identity for getting the current user.
		when(securityIdentity.getPrincipal())
			.thenReturn(testHelper.makePrincipal("principal-user"));

		when(securityResource.findUserByUsername("principal-user"))
			.thenReturn(testHelper.makeUser("principal-user", userId));

	}

	@Test
	void saveNew() {
		// Perform a save operation for a new dashboard.
		String dashboardId =
			dashboardService.saveNew(testHelper.makeDashboard("test dashboard"))
				.getId().toHexString();

		// Assert dashboard was persisted.
		assertTrue(dashboardService.findByIdOptional(dashboardId).isPresent());

	}

	@Test
	void saveUpdate() {
		// Perform a save operation for a new dashboard.
		String dashboardId =
			dashboardService.saveNew(testHelper.makeDashboard("test dashboard"))
				.getId().toHexString();

		// Find the dashboard and update values.
		DashboardEntity dashboard = dashboardService.findByIdOptional(dashboardId).orElseThrow();
		dashboard.setName("test dashboard updated");
		dashboardService.saveUpdate(dashboard);

		// Assert dashboard was updated.
		assertEquals("test dashboard updated",
			dashboardService.findByIdOptional(dashboardId).orElseThrow().getName());
	}

	@Test
	void deleteById() {
		// Perform a save operation for a new dashboard.
		String dashboardId =
			dashboardService.saveNew(testHelper.makeDashboard("test dashboard"))
				.getId().toHexString();

		// Perform a delete operation for the given dashboard ID.
		dashboardService.deleteById(dashboardId);

		// Assert dashboard was deleted.
		assertTrue(dashboardService.findByIdOptional(dashboardId).isEmpty());
	}

	@Test
	void findByIdOptional() {
		// Perform a save operation for a new dashboard.
		String dashboardId =
			dashboardService.saveNew(testHelper.makeDashboard("test dashboard"))
				.getId().toHexString();

		// Assert dashboard can be found.
		assertTrue(dashboardService.findByIdOptional(dashboardId).isPresent());
	}

	@Test
	void findAllForCurrentUser() {
		// Assert no dashboards are found for the current user.
		assertTrue(dashboardService.findAllForCurrentUser().isEmpty());

		// Perform a save operation for a new dashboard for the current user.
		dashboardService.saveNew(testHelper.makeDashboard("test dashboard").setOwnerId(userId));

		// Assert one dashboard is found for the current user.
		assertFalse(dashboardService.findAllForCurrentUser().isEmpty());

	}

	@Test
	void findShared() {
		// Perform a save operation for a new dashboard not shared.
		dashboardService.saveNew(testHelper.makeDashboard("test dashboard").setShared(false));

		// Assert no shared dashboards are found.
		assertTrue(dashboardService.findShared().isEmpty());

		// Perform a save operation for a new dashboard shared.
		dashboardService.saveNew(testHelper.makeDashboard("test dashboard").setShared(true));

		// Assert one shared dashboard is found.
		assertFalse(dashboardService.findShared().isEmpty());
	}
}
