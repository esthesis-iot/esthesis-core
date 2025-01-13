package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemSensorConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSensor;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSensor.DashboardUpdateSensorBuilder;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
public class SensorUpdateJobHelper extends UpdateJobHelper<DashboardUpdateSensor> {

	@Inject
	RedisUtils redisUtils;

	public DashboardUpdateSensor refresh(DashboardEntity dashboardEntity, DashboardItemDTO item) {
		DashboardUpdateSensorBuilder<?, ?> replyBuilder = DashboardUpdateSensor.builder()
			.id(item.getId()).type(Type.SENSOR);

		try {
			// Get item configuration & security checks.
			DashboardItemSensorConfiguration config = getConfig(DashboardItemSensorConfiguration.class,
				item);
			if (StringUtils.isBlank(config.getHardwareId()) || StringUtils.isBlank(
				config.getMeasurement())) {
				return replyBuilder.build();
			}
			if (!checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ,
				config.getHardwareId())) {
				return replyBuilder.isSecurityError(true).build();
			}

			// Get sensor value and return update.
			return replyBuilder.hardwareId(config.getHardwareId()).measurement(config.getMeasurement())
				.value(redisUtils.getFromHash(KeyType.ESTHESIS_DM, config.getHardwareId(),
					config.getMeasurement())).build();
		} catch (JsonProcessingException e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.SENSOR, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}
}
