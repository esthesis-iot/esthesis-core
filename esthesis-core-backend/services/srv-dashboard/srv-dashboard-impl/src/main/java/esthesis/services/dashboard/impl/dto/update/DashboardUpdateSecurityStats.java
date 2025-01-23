package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Data update for the SECURITY_STATS dashboard item type.
 */
@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DashboardUpdateSecurityStats extends DashboardUpdate {

	private long users;
	private long groups;
	private long roles;
	private long policies;
	private long audits;
}
