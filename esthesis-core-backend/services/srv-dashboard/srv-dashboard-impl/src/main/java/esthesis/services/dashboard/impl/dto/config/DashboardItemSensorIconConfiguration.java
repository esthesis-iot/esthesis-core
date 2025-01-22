package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

@Data
public class DashboardItemSensorIconConfiguration {

	private String hardwareId;
	private String measurement;
	private String unit;
	private int precision;

}
