package esthesis.services.dashboard.impl.dto.config;

import lombok.Data;

/**
 * Dashboard configuration for IMAGE dashboard item type.
 */
@Data
public class DashboardItemImageConfiguration {

	String imageUrl;
	Integer height;
	Integer refresh;
}
