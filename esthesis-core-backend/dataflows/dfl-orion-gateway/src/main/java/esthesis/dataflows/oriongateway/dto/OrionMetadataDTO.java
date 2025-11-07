package esthesis.dataflows.oriongateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Orion metadata information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrionMetadataDTO {
	private Object value;
	private String type;
}
