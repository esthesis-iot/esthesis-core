package esthesis.avro.util.kafka;

import esthesis.avro.EsthesisDataMessage;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class EsthesisDataMessageSerializer implements Serializer<EsthesisDataMessage> {

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		Serializer.super.configure(configs, isKey);
	}

	@Override
	@SneakyThrows
	public byte[] serialize(String topic, EsthesisDataMessage msg) {
		return EsthesisDataMessage.getEncoder().encode(msg).array();
	}

	@Override
	@SneakyThrows
	public byte[] serialize(String topic, Headers headers, EsthesisDataMessage msg) {
		return EsthesisDataMessage.getEncoder().encode(msg).array();
	}

	@Override
	public void close() {
		Serializer.super.close();
	}
}
