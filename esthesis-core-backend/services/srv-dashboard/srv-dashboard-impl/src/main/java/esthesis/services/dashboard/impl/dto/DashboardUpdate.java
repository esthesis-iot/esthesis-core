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

	// A flag indicating an error occurred while producing the value.
	private boolean isError;

	// A flag indicating a security error occurred while producing the value. When setting this value
	// to true, it is recommended to also set 'isError' to true, as some extensions might not be
	// interested to explicitly check for security errors, however they still want to know if
	// processing succeeded or not.
	private boolean isSecurityError;
}
