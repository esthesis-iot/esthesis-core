package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.dto.DevicesTotalsStatsDTO;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDevicesStatus;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDevicesStatus.DashboardUpdateDevicesStatusBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Helper class for updating the DEVICES_STATUS dashboard item.
 */
@Slf4j
@ApplicationScoped
public class DevicesStatusUpdateJobHelper extends UpdateJobHelper<DashboardUpdateDevicesStatus> {

	@Inject
	@RestClient
	DeviceSystemResource deviceSystemResource;

	public DashboardUpdateDevicesStatus refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateDevicesStatusBuilder<?, ?> replyBuilder =
			DashboardUpdateDevicesStatus.builder().id(item.getId()).type(Type.DEVICES_STATUS);

		try {
			if (!checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ, "*")) {
				return replyBuilder.isSecurityError(true).build();
			}

			// Get value and return update.
			DevicesTotalsStatsDTO stats = deviceSystemResource.getDeviceTotalsStats();
			return replyBuilder
				.total(stats.getTotal())
				.disabled(stats.getDisabled())
				.preregistered(stats.getPreregistered())
				.registered(stats.getRegistered())
				.build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.DEVICES_STATUS, item.getId(),
				e);
			return replyBuilder.isError(true).build();
		}
	}
}
