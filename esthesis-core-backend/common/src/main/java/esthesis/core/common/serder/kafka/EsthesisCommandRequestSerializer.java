package esthesis.core.common.serder.kafka;

import esthesis.common.avro.EsthesisCommandRequestMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Serializer for esthesis command request message. See esthesis-common/src/main/avro.
 */
@Slf4j
public class EsthesisCommandRequestSerializer implements Serializer<EsthesisCommandRequestMessage> {

	@Override
	@SneakyThrows
	public byte[] serialize(String topic, EsthesisCommandRequestMessage msg) {
		return EsthesisCommandRequestMessage.getEncoder().encode(msg).array();
	}
}
