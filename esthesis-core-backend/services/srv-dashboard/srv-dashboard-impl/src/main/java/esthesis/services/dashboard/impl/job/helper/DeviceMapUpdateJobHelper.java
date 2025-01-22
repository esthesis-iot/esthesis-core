package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.settings.resource.SettingsSystemResource;
import esthesis.services.dashboard.impl.dto.config.DashboardItemDeviceMapConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDeviceMap;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDeviceMap.DashboardUpdateDeviceMapBuilder;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DeviceMapUpdateJobHelper extends UpdateJobHelper<DashboardUpdateDeviceMap> {

	@Inject
	RedisUtils redisUtils;

	@Inject
	@RestClient
	DeviceSystemResource deviceSystemResource;

	@Inject
	@RestClient
	SettingsSystemResource settingsSystemResource;

	// The measurement names for latitude and longitude as configured in system settings.
	private String latMeasurement;
	private String lonMeasurement;

	@Override
	public void init() {
		latMeasurement = settingsSystemResource.findByName(NamedSetting.DEVICE_GEO_LAT).getValue();
		lonMeasurement = settingsSystemResource.findByName(NamedSetting.DEVICE_GEO_LON).getValue();
	}

	public DashboardUpdateDeviceMap refresh(DashboardEntity dashboardEntity, DashboardItemDTO item) {
		DashboardUpdateDeviceMapBuilder<?, ?> replyBuilder =
			DashboardUpdateDeviceMap.builder()
				.id(item.getId())
				.type(Type.DEVICE_MAP);

		try {
			// Get item configuration.
			DashboardItemDeviceMapConfiguration config =
				getConfig(DashboardItemDeviceMapConfiguration.class, item);
			if (config.getHardwareIds().length == 0 && config.getTags().length == 0) {
				return replyBuilder.build();
			}

			// Find the hardware IDs to be displayed on the map.
			List<String> hardwareIds = new ArrayList<>();
			if (config.getHardwareIds() != null && config.getHardwareIds().length > 0) {
				hardwareIds.addAll(List.of(config.getHardwareIds()));
			}
			if (config.getTags() != null && config.getTags().length > 0) {
				hardwareIds.addAll(deviceSystemResource.findByTagIds(String.join(",", config.getTags())));
			}
			hardwareIds = hardwareIds.stream().distinct().collect(Collectors.toList());

			if (hardwareIds.isEmpty()) {
				return replyBuilder.build();
			} else {
				// Get coordinates and return update.
				hardwareIds.forEach(hardwareId -> {
					if (checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ, hardwareId)) {
						String lat = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId, latMeasurement);
						String lon = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId, lonMeasurement);
						if (StringUtils.isNotBlank(lat) && StringUtils.isNotBlank(lon)) {
							replyBuilder.coordinate(String.join(",", hardwareId, lat, lon));
						}
					} else {
						// Although the caller would not know for which hardware ID the security check
						// failed, we should indicate that a security error occurred.
						replyBuilder.isSecurityError(true);
					}
				});

				return replyBuilder.build();
			}
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.DEVICE_MAP, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}
}
