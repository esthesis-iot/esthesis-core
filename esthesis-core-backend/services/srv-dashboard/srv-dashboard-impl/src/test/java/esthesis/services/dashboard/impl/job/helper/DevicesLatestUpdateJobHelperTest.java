package esthesis.services.dashboard.impl.job.helper;

import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.Dashboard.Type.DEVICES_LATEST;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@QuarkusTest
class DevicesLatestUpdateJobHelperTest {


	@Inject
	DevicesLatestUpdateJobHelper devicesLatestUpdateJobHelper;

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


		// Mock the latest devices.
		when(deviceSystemResource.getLatestDevices(anyInt()))
			.thenReturn(List.of(testHelper.makeCoreDevice("test-hardware")));

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem("test-item", 0, DEVICES_LATEST)
				.setConfiguration("{\"entries\": 1 }");

		// Assert that the refresh method returns a non-empty list of devices.
		assertFalse(devicesLatestUpdateJobHelper.refresh(dashboardEntity, item).getDevices().isEmpty());


	}

	@Test
	void refreshWithoutSecurity() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem("test-item", 0, DEVICES_LATEST)
				.setConfiguration("{\"entries\": 1 }");

		// Assert that the refresh method returns a security error.
		assertTrue(devicesLatestUpdateJobHelper.refresh(dashboardEntity, item).isSecurityError());

	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);


		// Mock the latest devices.
		when(deviceSystemResource.getLatestDevices(anyInt()))
			.thenReturn(List.of(testHelper.makeCoreDevice("test-hardware")));

		// Arrange the dashboard and item without required configuration.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DEVICES_LATEST);

		// Assert that the refresh method results in an error.
		assertTrue(devicesLatestUpdateJobHelper.refresh(dashboardEntity, item).isError());
	}
}
