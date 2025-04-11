package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsSystemResource;
import esthesis.services.dashboard.impl.TestHelper;
import esthesis.services.dashboard.impl.dto.config.DashboardItemDeviceMapConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDeviceMap;
import esthesis.util.redis.RedisUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static esthesis.core.common.AppConstants.Dashboard.Type.DEVICE_MAP;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_GEO_LAT;
import static esthesis.core.common.AppConstants.NamedSetting.DEVICE_GEO_LON;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class DeviceMapUpdateJobHelperTest {

	@Inject
	DeviceMapUpdateJobHelper deviceMapUpdateJobHelper;

	@Inject
	TestHelper testHelper;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	DeviceSystemResource deviceSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SettingsSystemResource settingsSystemResource;

	@RestClient
	@InjectMock
	@MockitoConfig(convertScopes = true)
	SecuritySystemResource securitySystemResource;


	@InjectMock
	RedisUtils redisUtils;

	@Test
	void init() {
		when(settingsSystemResource.findByName(any())).thenReturn(new SettingEntity("test", "test"));

		assertDoesNotThrow(() -> deviceMapUpdateJobHelper.init());
	}

	@Test
	void refresh() {

		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the lat and lon settings.
		when(settingsSystemResource.findByName(DEVICE_GEO_LAT)).thenReturn(new SettingEntity("lat", "lat"));
		when(settingsSystemResource.findByName(DEVICE_GEO_LON)).thenReturn(new SettingEntity("lon", "long"));

		// Mock the lat/lon extracted from redis cache.
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn("23.456");

		// Mock finding device hardware by tag ids.
		when(deviceSystemResource.findByTagIds(anyString())).thenReturn(List.of("hardware-test"));

		// Arrange a dashboard and a Device Map item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-map-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem(
				"test-map-item",
				0,
				DEVICE_MAP)
			.setConfiguration(createDashboardItemDeviceMapConfiguration());


		// Assert the map coordinates are updated.
		DashboardUpdateDeviceMap dashboardUpdateDeviceMap = deviceMapUpdateJobHelper.refresh(dashboardEntity, item);
		assertFalse(dashboardUpdateDeviceMap.getCoordinates().isEmpty());


	}

	@Test
	void refreshWithPermissionError() {

		// Mock the security as not permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(false);

		// Mock the lat and lon settings.
		when(settingsSystemResource.findByName(DEVICE_GEO_LAT)).thenReturn(new SettingEntity("lat", "lat"));
		when(settingsSystemResource.findByName(DEVICE_GEO_LON)).thenReturn(new SettingEntity("lon", "long"));

		// Mock the lat/lon extracted from redis cache.
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn("23.456");

		// Mock finding device hardware by tag ids.
		when(deviceSystemResource.findByTagIds(anyString())).thenReturn(List.of("hardware-test"));

		// Arrange a dashboard and a Device Map item.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-map-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem(
				"test-map-item",
				0,
				DEVICE_MAP)
			.setConfiguration(this.createDashboardItemDeviceMapConfiguration());


		// Assert no updates were made due to permission error.
		DashboardUpdateDeviceMap dashboardUpdateDeviceMap = deviceMapUpdateJobHelper.refresh(dashboardEntity, item);
		assertTrue(dashboardUpdateDeviceMap.getCoordinates().isEmpty());
		assertTrue(dashboardUpdateDeviceMap.isSecurityError());

	}

	@Test
	void refreshWithError() {

		// Mock the security as permitted.
		when(securitySystemResource.isPermitted(any(), any(), any(), any())).thenReturn(true);

		// Mock the lat and lon settings.
		when(settingsSystemResource.findByName(DEVICE_GEO_LAT)).thenReturn(new SettingEntity("lat", "lat"));
		when(settingsSystemResource.findByName(DEVICE_GEO_LON)).thenReturn(new SettingEntity("lon", "long"));

		// Mock the lat/lon extracted from redis cache.
		when(redisUtils.getFromHash(any(), anyString(), anyString())).thenReturn("23.456");

		// Mock finding device hardware by tag ids.
		when(deviceSystemResource.findByTagIds(anyString())).thenReturn(List.of("hardware-test"));

		// Arrange a dashboard and a Device Map item without required configuration.
		DashboardEntity dashboardEntity = testHelper.makeDashboard("test-map-dashboard");
		DashboardItemDTO item = testHelper.makeDashboardItem("test-map-item", 0, DEVICE_MAP);

		// Assert that the refresh method results in an error.
		assertTrue(deviceMapUpdateJobHelper.refresh(dashboardEntity, item).isError());

	}

	@SneakyThrows
	private String createDashboardItemDeviceMapConfiguration() {
		DashboardItemDeviceMapConfiguration config = new DashboardItemDeviceMapConfiguration();
		config.setMapLng("23.456");
		config.setMapLat("23.456");
		config.setZoom(10);
		config.setHeight(320);
		config.setTags(new String[]{"tag1", "tag2"});
		config.setHardwareIds(new String[]{"hardwareId1", "hardwareId2"});

		return new ObjectMapper().writeValueAsString(config);

	}
}
