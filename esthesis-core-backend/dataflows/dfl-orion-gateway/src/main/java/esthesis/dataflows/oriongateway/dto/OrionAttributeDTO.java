package esthesis.dataflows.oriongateway.dto;

import esthesis.common.data.DataUtils.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for representing an Orion attribute compatible with NGSI-V2 and NGSI-LD.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrionAttributeDTO {

	private Object value;

	private ValueType type;

	// Needed for NGSI-V2.
	private Map<String, OrionMetadataDTO> metadata = new HashMap<>();

	private boolean maintainedByEsthesis;
}
