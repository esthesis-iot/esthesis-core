package esthesis.service.device.dto;

import lombok.Data;

@Data
public class DevicesTotalsStatsDTO {

	// Total devices.
	private long total;
	// Total devices with status disabled.
	private long disabled;
	// Total devices with status preregistered.
	private long preregistered;
	// Total devices with status registered.
	private long registered;
}
