package esthesis.services.dashboard.impl.dto;

import esthesis.core.common.AppConstants;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * A super class representing dashboard update messages.
 */
@Data
@SuperBuilder
public abstract class DashboardUpdate {

	// The ID of the dashboard item belonging to a specific dashboard (as kept in the database).
	private String id;

	// The type of dashboard item this update is for.
	private AppConstants.Dashboard.Type type;
}
