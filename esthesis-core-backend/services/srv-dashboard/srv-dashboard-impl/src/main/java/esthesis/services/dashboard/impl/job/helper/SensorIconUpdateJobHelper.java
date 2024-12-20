package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemSensorIconConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSensorIcon;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
@Named("SensorIconUpdateJobHelper")
public class SensorIconUpdateJobHelper extends UpdateJobHelper<DashboardUpdateSensorIcon> {

	@Inject
	RedisUtils redisUtils;

	public DashboardUpdateSensorIcon refresh(DashboardEntity dashboardEntity, DashboardItemDTO item)
	throws JsonProcessingException {
		// Get item configuration & security checks.
		DashboardItemSensorIconConfiguration config = getConfig(
			DashboardItemSensorIconConfiguration.class, item);
		if (StringUtils.isBlank(config.getHardwareId()) || StringUtils.isBlank(
			config.getMeasurement())) {
			return null;
		}
		if (!checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ, config.getHardwareId())) {
			return null;
		}
		
		// Get sensor value and return update.
		return DashboardUpdateSensorIcon.builder()
			.id(item.getId())
			.type(Type.SENSOR_ICON)
			.hardwareId(config.getHardwareId())
			.measurement(config.getMeasurement())
			.value(redisUtils.getFromHash(KeyType.ESTHESIS_DM, config.getHardwareId(),
				config.getMeasurement()))
			.build();
	}
}
