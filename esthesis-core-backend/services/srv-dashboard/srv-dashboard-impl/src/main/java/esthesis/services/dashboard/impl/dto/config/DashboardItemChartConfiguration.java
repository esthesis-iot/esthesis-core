package esthesis.services.dashboard.impl.dto.config;

import java.util.List;
import lombok.Data;

@Data
public class DashboardItemChartConfiguration {

	private List<String> tags;
	private List<String> measurements;
	private List<String> hardwareIds;
}
