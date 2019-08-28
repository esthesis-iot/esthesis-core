package esthesis.platform.server.mapper;

import esthesis.common.datasink.MQTTDataEvent;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface MQTTMessageMapper {
  MQTTDataEvent map(MqttMessage mqttMessage);
}
