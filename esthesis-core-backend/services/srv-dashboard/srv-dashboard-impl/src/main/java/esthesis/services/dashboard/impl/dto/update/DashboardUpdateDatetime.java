package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Data update for the DATETIME dashboard item type.
 */
@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DashboardUpdateDatetime extends DashboardUpdate {

	private long serverDate;
}
