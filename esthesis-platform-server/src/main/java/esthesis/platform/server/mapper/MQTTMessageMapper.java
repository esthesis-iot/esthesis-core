package esthesis.platform.server.mapper;

import esthesis.platform.datasink.common.MQTTMetadataEvent;
import esthesis.platform.datasink.common.MQTTTelemetryEvent;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface MQTTMessageMapper {
  MQTTTelemetryEvent mapToTelemetryEvent(MqttMessage mqttMessage);
  MQTTMetadataEvent mapToMetadataEvent(MqttMessage mqttMessage);
}
