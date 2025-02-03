package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

/**
 * Dashboard configuration for SENSOR_ICON dashboard item type.
 */
@Data
public class DashboardItemSensorIconConfiguration {

	private String hardwareId;
	private String measurement;
	private String unit;
	private int precision;

}
