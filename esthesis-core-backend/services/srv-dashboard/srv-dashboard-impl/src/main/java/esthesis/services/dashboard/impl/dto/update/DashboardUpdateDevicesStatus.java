package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DashboardUpdateDevicesStatus extends DashboardUpdate {

	private long disabled;
	private long preregistered;
	private long registered;
	private long total;
}
