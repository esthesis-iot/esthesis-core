package esthesis.dataflows.oriongateway.dto;

import esthesis.service.device.entity.DeviceAttributeEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for representing an Orion entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrionEntityDTO {

	private String type;
	private String id;

	private List<OrionAttributeDTO> attributes = new ArrayList<>();

	public static List<OrionAttributeDTO> attributesFromEsthesisDeviceAttributes(
		List<DeviceAttributeEntity> esthesisAttributes) {
		List<OrionAttributeDTO> orionAttributes = new ArrayList<>();
		for (DeviceAttributeEntity esthesisAttribute : esthesisAttributes) {
			orionAttributes.add(OrionAttributeDTO.builder()
				.name(esthesisAttribute.getAttributeName())
				.value(esthesisAttribute.getAttributeValue())
				.maintainedByEsthesis(true)
				.build());
		}

		return orionAttributes;
	}
}
