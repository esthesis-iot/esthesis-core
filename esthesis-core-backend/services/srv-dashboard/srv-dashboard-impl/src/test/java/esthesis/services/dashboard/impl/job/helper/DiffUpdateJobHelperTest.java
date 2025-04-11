package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.config.DashboardItemSensorConfiguration;
import esthesis.util.redis.RedisUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static esthesis.core.common.AppConstants.Dashboard.Type.DIFF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class DiffUpdateJobHelperTest {

	@Inject
	DiffUpdateJobHelper diffUpdateJobHelper;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;

	@Inject
	TestHelper testHelper;

	@InjectMock
	RedisUtils redisUtils;

	@Test
	void refresh() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the Redis cache to return a test value.
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn("test-value");

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DIFF);
		item.setConfiguration(createSensorConfiguration());


		// Assert that the refresh method has set the expected value.
		assertEquals("test-value", diffUpdateJobHelper.refresh(dashboardEntity, item).getValue());
	}

	@Test
	void refreshWithoutSecurity() {
		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Arrange the dashboard and item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DIFF);
		item.setConfiguration(createSensorConfiguration());

		// Assert that the refresh method returns a security error.
		assertTrue(diffUpdateJobHelper.refresh(dashboardEntity, item).isSecurityError());
	}

	@Test
	void refreshWithError() {
		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the Redis cache to return a test value.
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn("test-value");

		// Arrange the dashboard and item without required configuration.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-item", 0, DIFF);

		// Assert that the refresh method results in an error.
		assertTrue(diffUpdateJobHelper.refresh(dashboardEntity, item).isError());
	}


	@SneakyThrows
	private String createSensorConfiguration() {
		DashboardItemSensorConfiguration sensorConfiguration = new DashboardItemSensorConfiguration();
		sensorConfiguration.setHardwareId("test-hardware-id");
		sensorConfiguration.setMeasurement("test-measurement");
		sensorConfiguration.setUnit("test-unit");
		return new ObjectMapper().writeValueAsString(sensorConfiguration);
	}
}
