package esthesis.dataflows.oriongateway;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.PayloadData;
import esthesis.common.data.DataUtils.ValueType;
import esthesis.common.util.EsthesisCommonConstants.Device.Type;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.dataflows.oriongateway.dto.OrionAttributeDTO;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class TestHelper {

	public DeviceEntity createDeviceEntity(String hardwareId) {
		DeviceEntity device =
			new DeviceEntity()
				.setHardwareId(hardwareId)
				.setCreatedOn(Instant.now())
				.setType(Type.CORE)
				.setTags(List.of("test-tag"))
				.setLastSeen(Instant.now())
				.setStatus(Status.REGISTERED);
		device.setId(new ObjectId());
		return device;
	}

	public OrionEntityDTO createOrionEntity(String orionId) {
		OrionEntityDTO orionEntity = new OrionEntityDTO();
		orionEntity.setId(orionId);
		orionEntity.setType("test-type");

		Map<String, OrionAttributeDTO> attributes = new HashMap<>();
		attributes.put("test-attribute", OrionAttributeDTO.builder()
			.value("test-value")
			.type(ValueType.STRING)
			.build());

		orionEntity.setAttributes(attributes);
		return orionEntity;
	}

	public DeviceAttributeEntity createDeviceAttributeEntity(
		String deviceId,
		String attributeName,
		String attributeValue,
		ValueType attributeType
	) {
		DeviceAttributeEntity deviceAttribute = new DeviceAttributeEntity();
		deviceAttribute.setId(new ObjectId());
		deviceAttribute.setDeviceId(deviceId);
		deviceAttribute.setAttributeName(attributeName);
		deviceAttribute.setAttributeValue(attributeValue);
		deviceAttribute.setAttributeType(attributeType);
		return deviceAttribute;
	}

	public EsthesisDataMessage createEsthesisDataMessage(String hardwareId, PayloadData payloadData) {
		EsthesisDataMessage esthesisDataMessage = new EsthesisDataMessage();
		esthesisDataMessage.setChannel("test-channel");
		esthesisDataMessage.setId("test-id");
		esthesisDataMessage.setHardwareId(hardwareId);
		esthesisDataMessage.setType(MessageTypeEnum.T);
		esthesisDataMessage.setSeenAt(Instant.now().toString());
		esthesisDataMessage.setSeenBy("test-seen-by");
		esthesisDataMessage.setPayload(payloadData);
		return esthesisDataMessage;
	}

	public Map<String, Object> createOrionEntityMap(OrionEntityDTO orionEntity) {
		// Prepare the attributes map.
		Map<String, Object> attributesMap = orionEntity.getAttributes().entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> {
				OrionAttributeDTO attr = entry.getValue();
				Map<String, Object> attrMap = new HashMap<>();

				// 🔥 Dynamically add all non-null fields.
				for (Field field : OrionAttributeDTO.class.getDeclaredFields()) {
					// Allow access to private fields.
					field.setAccessible(true);
					try {
						Object value = field.get(attr);
						if (value != null) {
							attrMap.put(field.getName(), value);
						}
					} catch (IllegalAccessException e) {
						throw new RuntimeException("Error accessing field: " + field.getName(), e);
					}
				}

				return attrMap;
			}));

		// Prepare the Orion entity map.
		Map<String, Object> orionEntityMap = new HashMap<>();
		orionEntityMap.put("id", orionEntity.getId());
		orionEntityMap.put("type", orionEntity.getType());
		orionEntityMap.putAll(attributesMap);

		return orionEntityMap;
	}
}
