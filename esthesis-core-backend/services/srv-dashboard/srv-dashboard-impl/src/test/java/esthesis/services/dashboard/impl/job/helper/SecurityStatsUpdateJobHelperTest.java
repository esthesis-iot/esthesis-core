package esthesis.services.dashboard.impl.job.helper;

import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.dto.StatsDTO;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSecurityStats;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.Dashboard.Type.CHART;
import static esthesis.core.common.AppConstants.Dashboard.Type.SECURITY_STATS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class SecurityStatsUpdateJobHelperTest {

	@Inject
	SecurityStatsUpdateJobHelper securityStatsUpdateJobHelper;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;

	@Inject
	TestHelper testHelper;

	@Test
	void refresh() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock security stats response.
		StatsDTO stats = StatsDTO.builder().users(1L).roles(1L).audits(1L).policies(1L).build();
		when(securitySystemResource.stats()).thenReturn(stats);

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, SECURITY_STATS);


		// Assert that the refresh method returns the expected security stats.
		DashboardUpdateSecurityStats dashboardUpdateSecurityStats =
			securityStatsUpdateJobHelper.refresh(dashboardEntity, item);
		assertEquals(1L, dashboardUpdateSecurityStats.getUsers());
		assertEquals(1L, dashboardUpdateSecurityStats.getRoles());
		assertEquals(1L, dashboardUpdateSecurityStats.getAudits());
		assertEquals(1L, dashboardUpdateSecurityStats.getPolicies());
	}

	@Test
	void refreshWithoutSecurity() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, CHART);

		// Assert that the refresh method returns a security error.
		assertTrue(securityStatsUpdateJobHelper.refresh(dashboardEntity, item).isSecurityError());

	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the security stats response to throw an error.
		when(securitySystemResource.stats()).thenThrow(new RuntimeException("test error"));

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, SECURITY_STATS);

		// Assert that the refresh method results in an error.
		assertTrue(securityStatsUpdateJobHelper.refresh(dashboardEntity, item).isError());
	}

}
