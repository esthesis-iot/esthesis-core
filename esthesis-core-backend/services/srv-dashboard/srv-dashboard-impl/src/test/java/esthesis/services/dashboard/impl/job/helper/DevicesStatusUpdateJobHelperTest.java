package esthesis.services.dashboard.impl.job.helper;

import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.dto.DevicesTotalsStatsDTO;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDevicesStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.Dashboard.Type.DEVICES_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class DevicesStatusUpdateJobHelperTest {

	@Inject
	DevicesStatusUpdateJobHelper devicesStatusUpdateJobHelper;

	@RestClient
	@InjectMock
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


		// Arrange and mock the device total stats.
		DevicesTotalsStatsDTO stats = new DevicesTotalsStatsDTO();
		stats.setDisabled(0L);
		stats.setRegistered(1L);
		stats.setPreregistered(1L);
		stats.setTotal(10L);

		when(deviceSystemResource.getDeviceTotalsStats()).thenReturn(stats);


		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DEVICES_STATUS);

		// Assert the refresh method updates the dashboard item with the correct values.
		DashboardUpdateDevicesStatus dashboardUpdateDevicesStatus = devicesStatusUpdateJobHelper.refresh(dashboardEntity, item);
		assertEquals(stats.getRegistered(), dashboardUpdateDevicesStatus.getRegistered());
		assertEquals(stats.getPreregistered(), dashboardUpdateDevicesStatus.getPreregistered());
		assertEquals(stats.getDisabled(), dashboardUpdateDevicesStatus.getDisabled());
		assertEquals(stats.getTotal(), dashboardUpdateDevicesStatus.getTotal());

	}

	@Test
	void refreshWithoutSecurity() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DEVICES_STATUS);

		// Assert that the refresh method returns a security error.
		assertTrue(devicesStatusUpdateJobHelper.refresh(dashboardEntity, item).isSecurityError());
	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);


		// Mock the device total stats to throw an exception.
		when(deviceSystemResource.getDeviceTotalsStats()).thenThrow(new RuntimeException("Test exception"));


		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DEVICES_STATUS);

		// Assert that the refresh method results in an error.
		assertTrue(devicesStatusUpdateJobHelper.refresh(dashboardEntity, item).isError());

	}
}
