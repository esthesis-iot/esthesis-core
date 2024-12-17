package esthesis.services.dashboard.impl.dto;

import esthesis.core.common.AppConstants;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class DashboardUpdate {

	private String id;
	private AppConstants.Dashboard.Type type;
}
