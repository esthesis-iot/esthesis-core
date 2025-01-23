package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Data update for the NOTES dashboard item type.
 */
@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DashboardUpdateNotes extends DashboardUpdate {

	private String notes;
}
