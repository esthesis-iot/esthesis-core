package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

@Data
public class DashboardItemSensorConfiguration {

	private String hardwareId;
	private String measurement;
	private String unit;
	private String icon;
	private int precision;
	private boolean sparkline;
	private int sparkline_points;
}
