package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

/**
 * Dashboard configuration for DEVICES_LATEST dashboard item type.
 */
@Data
public class DashboardItemDevicesLatestConfiguration {

	// The number of devices to display.
	int entries;
}
