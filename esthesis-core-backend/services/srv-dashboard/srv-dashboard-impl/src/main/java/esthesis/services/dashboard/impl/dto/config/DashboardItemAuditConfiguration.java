package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

/**
 * Dashboard configuration for AUDIT dashboard item type.
 */
@Data
public class DashboardItemAuditConfiguration {

	// The number of audit entries to display.
	int entries;
}
