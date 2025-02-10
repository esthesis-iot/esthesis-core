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

import java.time.Instant;
import java.util.List;

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
		orionEntity.setAttributes(List.of(createOrionAttribute("test-attribute", "test-value")));
		return orionEntity;
	}

	public OrionAttributeDTO createOrionAttribute(String name, String value) {
		OrionAttributeDTO attribute = new OrionAttributeDTO();
		attribute.setName(name);
		attribute.setValue(value);
		return attribute;
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
		EsthesisDataMessage esthesisDataMessage =  new EsthesisDataMessage();
		esthesisDataMessage.setChannel("test-channel");
		esthesisDataMessage.setId("test-id");
		esthesisDataMessage.setHardwareId(hardwareId);
		esthesisDataMessage.setType(MessageTypeEnum.T);
		esthesisDataMessage.setSeenAt(Instant.now().toString());
		esthesisDataMessage.setSeenBy("test-seen-by");
		esthesisDataMessage.setPayload(payloadData);
		return esthesisDataMessage;
	}
}
