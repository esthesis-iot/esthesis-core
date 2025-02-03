package esthesis.service.device.dto;

import lombok.Data;

/**
 * A representation of the data needed to import device data.
 */
@Data
public class DeviceDataImportDTO {

	// The number of lines to process at once.
	private int batchSize;

	// The delay between batches, in milliseconds.
	private int batchDelay;
}
