package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DashboardUpdateDeviceMap extends DashboardUpdate {

	// The coordinates list contains triplets in the form of [hardwareId, latitude, longitude].
	@Singular
	private List<String> coordinates;
}
