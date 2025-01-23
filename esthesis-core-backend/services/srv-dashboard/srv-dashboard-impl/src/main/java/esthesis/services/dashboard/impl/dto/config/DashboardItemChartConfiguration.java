package esthesis.services.dashboard.impl.dto.config;

import java.util.List;
import lombok.Data;

/**
 * Dashboard configuration for CHART dashboard item type.
 */
@Data
public class DashboardItemChartConfiguration {

	private List<String> tags;
	private List<String> measurements;
	private List<String> hardwareIds;
}
