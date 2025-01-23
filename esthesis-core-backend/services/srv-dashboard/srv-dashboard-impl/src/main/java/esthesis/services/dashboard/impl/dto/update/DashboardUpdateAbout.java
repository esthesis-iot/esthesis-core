package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Data update for the ABOUT dashboard item type.
 */
@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DashboardUpdateAbout extends DashboardUpdate {

	private String gitVersion;
	private String gitBuildTime;
	private String gitCommitIdAbbrev;
	private String gitCommitId;
}
