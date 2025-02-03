package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.dto.DevicesLastSeenStatsDTO;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDevicesLastSeen;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDevicesLastSeen.DashboardUpdateDevicesLastSeenBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Helper class for updating the DEVICES_LAST_SEEN dashboard item.
 */
@Slf4j
@ApplicationScoped
public class DevicesLastSeenUpdateJobHelper extends
	UpdateJobHelper<DashboardUpdateDevicesLastSeen> {

	@Inject
	@RestClient
	DeviceSystemResource deviceSystemResource;

	public DashboardUpdateDevicesLastSeen refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateDevicesLastSeenBuilder<?, ?> replyBuilder = DashboardUpdateDevicesLastSeen.builder()
			.id(item.getId())
			.type(Type.DEVICES_LAST_SEEN);

		try {
			// Security checks.
			if (!checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ, "")) {
				return replyBuilder.isSecurityError(true).isError(true).build();
			}

			// Get data.
			DevicesLastSeenStatsDTO deviceStats = deviceSystemResource.getDeviceStats();

			// Return update.
			return replyBuilder
				.lastMonth(deviceStats.getSeenLastMonth())
				.lastWeek(deviceStats.getSeenLastWeek())
				.lastDay(deviceStats.getSeenLastDay())
				.lastHour(deviceStats.getSeenLastHour())
				.lastMinute(deviceStats.getSeenLastMinute())
				.build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.DEVICES_LAST_SEEN,
				item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}
}
