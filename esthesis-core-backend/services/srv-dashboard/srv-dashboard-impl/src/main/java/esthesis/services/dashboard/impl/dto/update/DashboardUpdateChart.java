package esthesis.services.dashboard.impl.dto.update;

import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Triple;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class DashboardUpdateChart extends DashboardUpdate {

	// The triple represents: hardware ID - measurement name - data
	List<Triple<String, String, String>> data;
}
