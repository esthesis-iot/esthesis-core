package esthesis.service.device.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A representation of the data needed to import device data in text format.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceTextDataImportDTO extends DeviceDataImportDTO {

	// The data to import in eLP format.
	private String data;
}
