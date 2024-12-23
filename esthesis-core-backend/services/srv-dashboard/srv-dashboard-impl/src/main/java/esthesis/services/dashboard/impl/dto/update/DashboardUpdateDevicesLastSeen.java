package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DashboardUpdateDevicesLastSeen extends DashboardUpdate {

	private long lastMonth;
	private long lastWeek;
	private long lastDay;
	private long lastHour;
	private long lastMinute;
}
