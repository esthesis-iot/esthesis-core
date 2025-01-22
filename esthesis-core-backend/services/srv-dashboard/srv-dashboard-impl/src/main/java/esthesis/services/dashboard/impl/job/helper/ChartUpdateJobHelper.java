package esthesis.services.dashboard.impl.job.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.services.dashboard.impl.dto.config.DashboardItemChartConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateChart;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateChart.DashboardUpdateChartBuilder;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
//@Dependent
@ApplicationScoped
public class ChartUpdateJobHelper extends UpdateJobHelper<DashboardUpdateChart> {

	@Inject
	RedisUtils redisUtils;

	@Inject
	@RestClient
	DeviceSystemResource deviceSystemResource;

	public DashboardUpdateChart refresh(DashboardEntity dashboardEntity, DashboardItemDTO item) {

		DashboardUpdateChartBuilder<?, ?> replyBuilder = DashboardUpdateChart.builder()
			.id(item.getId()).type(Type.CHART);

		try {
			// Get item configuration & security checks.
			DashboardItemChartConfiguration config = getConfig(DashboardItemChartConfiguration.class,
				item);

			// Return an empty response if no measurements are configured.
			if (config.getMeasurements().isEmpty()) {
				return replyBuilder.isError(true).build();
			}

			// Find the hardware IDs to be displayed on the chart.
			List<String> hardwareIds = new ArrayList<>();
			if (config.getHardwareIds() != null && !config.getHardwareIds().isEmpty()) {
				hardwareIds = config.getHardwareIds();
			}
			if (config.getTags() != null && !config.getTags().isEmpty()) {
				hardwareIds.addAll(deviceSystemResource.findByTagIds(
					String.join(",", config.getTags())));
			}
			hardwareIds = hardwareIds.stream().distinct().sorted().collect(Collectors.toList());

			// Return an empty response if no hardware IDs are found.
			if (hardwareIds.isEmpty()) {
				return replyBuilder.build();
			} else {
				// Get measurements.
				List<Triple<String, String, String>> data = new ArrayList<>();
				hardwareIds.forEach(hardwareId -> {
					if (checkSecurity(dashboardEntity, Category.DEVICE, Operation.READ, hardwareId)) {
						config.getMeasurements().forEach(measurement ->
							data.add(Triple.of(hardwareId, measurement,
								redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId, measurement))));
					}
				});

				return replyBuilder.data(data).build();
			}
		} catch (JsonProcessingException e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.CHART, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}
}
