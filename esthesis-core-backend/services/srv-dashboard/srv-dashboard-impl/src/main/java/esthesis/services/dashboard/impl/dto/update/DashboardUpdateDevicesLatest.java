package esthesis.services.dashboard.impl.dto.update;

import esthesis.common.util.EsthesisCommonConstants;
import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Data update for the DEVICES_LATEST dashboard item type.
 */
@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DashboardUpdateDevicesLatest extends DashboardUpdate {

	private List<DTO> devices;

	@Data
	@Builder
	public static class DTO {

		private String hardwareId;
		private Instant registeredOn;
		private EsthesisCommonConstants.Device.Type type;
	}
}
