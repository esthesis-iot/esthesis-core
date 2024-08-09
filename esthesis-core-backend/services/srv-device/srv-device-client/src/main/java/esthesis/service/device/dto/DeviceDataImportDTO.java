package esthesis.service.device.dto;

import lombok.Data;

@Data
public class DeviceDataImportDTO {
	// The number of lines to process at once.
	private int batchSize;

	// The delay between batches, in milliseconds.
	private int batchDelay;
}
