package esthesis.service.device.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceTextDataImportDTO extends DeviceDataImportDTO{
	// The data to import in eLP format.
	private String data;
}
