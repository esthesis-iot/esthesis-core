package esthesis.services.dashboard.impl.job.helper;

import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.util.redis.RedisUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.Dashboard.Type.CHART;
import static io.smallrye.common.constraint.Assert.assertFalse;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@Slf4j
class ChartUpdateJobHelperTest {

	@Inject
	ChartUpdateJobHelper chartUpdateJobHelper;

	@Inject
	TestHelper testHelper;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	DeviceSystemResource deviceSystemResource;

	@InjectMock
	RedisUtils redisUtils;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;


	@Test
	void refreshWithSecurityError() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange a dashboard and a Chart item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-chart-dashboard");
		DashboardItemDTO item =
			testHelper.makeDashboardItem("test-chart-item",
					0,
					CHART)
				.setConfiguration(
					"{\"tags\": [\"tag1\"], \"hardwareIds\": [\"hardwareId1\"], \"measurements\": [\"measurement1\"]}");


		// Assert  no data is returned due to security check failure.
		assertTrue(chartUpdateJobHelper.refresh(dashboardEntity, item).getData().isEmpty());
	}

	@Test
	void refresh() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the finding of hardware IDs by tag IDs.
		when(deviceSystemResource.findByTagIds(anyString())).thenReturn(List.of("test-hardware"));
		// Mock the Redis cache to return the expected data.
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn("test-data");

		// Arrange a dashboard and a CHART item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-chart-dashboard");
		DashboardItemDTO item =	testHelper.makeDashboardItem("test-chart-item",0,CHART);

		// Assert the chart item was updated correctly.
		assertFalse(chartUpdateJobHelper.refresh(dashboardEntity, item).getData().isEmpty());

	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Arrange a dashboard and a Chart item without required configuration.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-chart-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-chart-item", 0, CHART).setConfiguration("");


		// Assert that the refresh method results in an error.
		assertTrue(chartUpdateJobHelper.refresh(dashboardEntity, item).isError());
	}
}
