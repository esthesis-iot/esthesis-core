package esthesis.dataflows.oriongateway.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import esthesis.service.device.entity.DeviceAttributeEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for representing an Orion entity compatible with NGSI-V2 and NGSI-LD.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrionEntityDTO {

	private String type;
	private String id;

	// Store attributes but prevent them from appearing as a field in JSON.
	@JsonIgnore
	private Map<String, OrionAttributeDTO> attributes = new HashMap<>();

	// This will place the attributes directly at the root of the JSON object.
	@JsonAnyGetter
	public Map<String, OrionAttributeDTO> getAttributesAsJson() {
		return attributes;
	}

	public static Map<String, OrionAttributeDTO> attributesFromEsthesisDeviceAttributes(
		List<DeviceAttributeEntity> esthesisAttributes) {
		Map<String, OrionAttributeDTO> orionAttributes = new HashMap<>();
		for (DeviceAttributeEntity esthesisAttribute : esthesisAttributes) {
			orionAttributes.put(
				esthesisAttribute.getAttributeName(),
				OrionAttributeDTO.builder()
					.value(esthesisAttribute.getAttributeValue())
					.maintainedByEsthesis(true)
					.build()
			);
		}
		return orionAttributes;
	}
}
