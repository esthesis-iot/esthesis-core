package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.services.dashboard.impl.dto.config.DashboardItemDevicesLatestConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDevicesLatest;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateDevicesLatest.DashboardUpdateDevicesLatestBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;


@Slf4j
@ApplicationScoped
public class DevicesLatestUpdateJobHelper extends UpdateJobHelper<DashboardUpdateDevicesLatest> {

	@Inject
	@RestClient
	DeviceSystemResource deviceSystemResource;

	public DashboardUpdateDevicesLatest refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateDevicesLatestBuilder<?, ?> replyBuilder = DashboardUpdateDevicesLatest.builder()
			.id(item.getId())
			.type(Type.DEVICES_LAST_SEEN);

		try {
			// Get item configuration & security checks.
			DashboardItemDevicesLatestConfiguration config = getConfig(
				DashboardItemDevicesLatestConfiguration.class, item);
			if (!checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ, "")) {
				return replyBuilder.isSecurityError(true).isError(true).build();
			}

			// Get data.
			List<DeviceEntity> latestDevices = deviceSystemResource.getLatestDevices(config.getEntries());

			// Return update.
			return replyBuilder
				.devices(latestDevices.stream()
					.map(device -> DashboardUpdateDevicesLatest.DTO.builder()
						.hardwareId(device.getHardwareId())
						.registeredOn(device.getRegisteredOn())
						.type(device.getType())
						.build())
					.toList())
				.build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.DEVICES_LATEST, item.getId(),
				e);
			return replyBuilder.isError(true).build();
		}
	}
}
