package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Data update for the DIFF dashboard item type.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class DashboardUpdateDiff extends DashboardUpdate {

	private String hardwareId;
	private String measurement;
	private String value;
}
