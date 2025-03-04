package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

/**
 * Dashboard configuration for DIFF dashboard item type.
 */
@Data
public class DashboardItemDiffConfiguration {

	private String hardwareId;
	private String measurement;
	private String unit;
	private String icon;
	private long items;

}
