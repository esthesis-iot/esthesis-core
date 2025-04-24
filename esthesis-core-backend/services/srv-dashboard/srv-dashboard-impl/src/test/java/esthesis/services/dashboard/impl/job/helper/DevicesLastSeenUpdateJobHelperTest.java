package esthesis.services.dashboard.impl.job.helper;

import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.dto.DevicesLastSeenStatsDTO;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDevicesLastSeen;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.Dashboard.Type.DEVICES_LAST_SEEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class DevicesLastSeenUpdateJobHelperTest {


	@Inject
	DevicesLastSeenUpdateJobHelper devicesLastSeenUpdateJobHelper;

	@InjectMock
	@RestClient
	@MockitoConfig(convertScopes = true)
	DeviceSystemResource deviceSystemResource;

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

		// Arrange and mock the device stats.
		DevicesLastSeenStatsDTO stats = testHelper.makeDeviceLastSeenStats();
		when(deviceSystemResource.getDeviceStats()).thenReturn(stats);


		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DEVICES_LAST_SEEN);

		DashboardUpdateDevicesLastSeen dashboardUpdateDevicesLastSeen =
			devicesLastSeenUpdateJobHelper.refresh(dashboardEntity, item);

		// Assert the update occurred as expected.
		assertEquals(stats.getSeenLastDay(), dashboardUpdateDevicesLastSeen.getLastDay());
		assertEquals(stats.getSeenLastWeek(), dashboardUpdateDevicesLastSeen.getLastWeek());
		assertEquals(stats.getSeenLastMonth(), dashboardUpdateDevicesLastSeen.getLastMonth());
		assertEquals(stats.getSeenLastHour(), dashboardUpdateDevicesLastSeen.getLastHour());
		assertEquals(stats.getSeenLastMinute(), dashboardUpdateDevicesLastSeen.getLastMinute());
	}

	@Test
	void refreshWithoutSecurity() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DEVICES_LAST_SEEN);

		// Assert that the refresh method returns a security error.
		assertTrue(devicesLastSeenUpdateJobHelper.refresh(dashboardEntity, item).isSecurityError());
	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the device stats to throw an exception.
		when(deviceSystemResource.getDeviceStats()).thenThrow(new RuntimeException("Test exception"));


		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DEVICES_LAST_SEEN);

		// Assert that the refresh method results in an error.
		assertTrue(devicesLastSeenUpdateJobHelper.refresh(dashboardEntity, item).isError());
	}
}
