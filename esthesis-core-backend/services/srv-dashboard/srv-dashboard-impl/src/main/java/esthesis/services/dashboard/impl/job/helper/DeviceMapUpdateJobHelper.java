package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.NamedSetting;
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
		try {
			// Get item configuration & security checks.
			DashboardItemDeviceMapConfiguration config =
				getConfig(DashboardItemDeviceMapConfiguration.class, item);
			if (config.getHardwareIds().length == 0 && config.getTags().length == 0) {
				return null;
			}

			// Find the hardware IDs to be displayed on the map.
			List<String> hardwareIds = new ArrayList<>(List.of(config.getHardwareIds()));
			if (config.getTags() != null && config.getTags().length > 0) {
				hardwareIds.addAll(
					deviceSystemResource.findByTagNames(String.join(",", config.getTags())));
			}
			hardwareIds = hardwareIds.stream().distinct().collect(Collectors.toList());

			//TODO
//			if (!checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ,
//				config.getHardwareId())) {
//				return null;
//			}

			// Get coordinates and return update.
			DashboardUpdateDeviceMapBuilder<?, ?> builder = DashboardUpdateDeviceMap.builder()
				.id(item.getId())
				.type(Type.DEVICE_MAP);

			hardwareIds.forEach(hardwareId -> {
				String lat = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId, latMeasurement);
				String lon = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId, lonMeasurement);
				if (StringUtils.isNotBlank(lat) && StringUtils.isNotBlank(lon)) {
					builder.coordinate(String.join(",", hardwareId, lat, lon));
				}
			});

			return builder.build();
		} catch (JsonProcessingException e) {
			log.error("Error parsing configuration for '{}' item with id '{}'.",
				Type.SENSOR, item.getId(), e);
			return null;
		}
	}
}
