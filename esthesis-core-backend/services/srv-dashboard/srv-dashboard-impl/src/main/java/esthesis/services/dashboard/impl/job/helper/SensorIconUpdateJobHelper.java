package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemSensorIconConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSensorIcon;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSensorIcon.DashboardUpdateSensorIconBuilder;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for updating the SENSOR_ICON dashboard item.
 */
@Slf4j
@ApplicationScoped
public class SensorIconUpdateJobHelper extends UpdateJobHelper<DashboardUpdateSensorIcon> {

	@Inject
	RedisUtils redisUtils;

	public DashboardUpdateSensorIcon refresh(DashboardEntity dashboardEntity, DashboardItemDTO item) {
		DashboardUpdateSensorIconBuilder<?, ?> replyBuilder = DashboardUpdateSensorIcon.builder()
			.id(item.getId())
			.type(Type.SENSOR_ICON);

		try {
			// Get item configuration & security checks.
			DashboardItemSensorIconConfiguration config = getConfig(
				DashboardItemSensorIconConfiguration.class, item);
			if (StringUtils.isBlank(config.getHardwareId()) || StringUtils.isBlank(
				config.getMeasurement())) {
				return replyBuilder.build();
			}
			if (!checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ,
				config.getHardwareId())) {
				return replyBuilder.isSecurityError(true).build();
			}

			// Get sensor value and return update.
			return replyBuilder
				.hardwareId(config.getHardwareId())
				.measurement(config.getMeasurement())
				.value(redisUtils.getFromHash(KeyType.ESTHESIS_DM, config.getHardwareId(),
					config.getMeasurement()))
				.build();
		} catch (JsonProcessingException e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.SENSOR_ICON, item.getId(),
				e);
			return replyBuilder.isError(true).build();
		}
	}
}
