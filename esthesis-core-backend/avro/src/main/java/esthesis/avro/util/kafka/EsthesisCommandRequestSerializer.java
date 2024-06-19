package esthesis.avro.util.kafka;

import esthesis.avro.EsthesisCommandRequestMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class EsthesisCommandRequestSerializer implements Serializer<EsthesisCommandRequestMessage> {

	@Override
	@SneakyThrows
	public byte[] serialize(String topic, EsthesisCommandRequestMessage msg) {
		return EsthesisCommandRequestMessage.getEncoder().encode(msg).array();
	}
}
