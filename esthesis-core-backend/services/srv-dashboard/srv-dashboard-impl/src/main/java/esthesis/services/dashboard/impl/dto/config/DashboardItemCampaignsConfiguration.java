package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

/**
 * Dashboard configuration for CAMPAIGN dashboard item type.
 */
@Data
public class DashboardItemCampaignsConfiguration {

	// The number of campaign entries to display (starting from the newest).
	int entries;
}
