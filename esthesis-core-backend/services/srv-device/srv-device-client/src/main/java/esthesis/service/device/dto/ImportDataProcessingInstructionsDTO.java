package esthesis.service.device.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ImportDataProcessingInstructionsDTO {
	// The number of records to process in a single batch.
	private int batchSize = Integer.MAX_VALUE;
	// The delay in milliseconds between batches.
	private Integer batchDelay = 0;
}
