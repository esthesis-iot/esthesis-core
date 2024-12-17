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
	private int sparklinePoints;
	private boolean threshold;
	private double thresholdLow;
	private String thresholdLowColor;
	private double thresholdMiddle;
	private String thresholdMiddleColor;
	private double thresholdHigh;
	private String thresholdHighColor;

}
