package esthesis.dataflow.mqttclient.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * Test container for HiveMQ MQTT broker used in dataflow integration tests.
 */
@Slf4j
public class HiveMQTC implements QuarkusTestResourceLifecycleManager {

	HiveMQContainer container;

	@Override
	public Map<String, String> start() {
		container = new HiveMQContainer(
			DockerImageName.parse("hivemq/hivemq-ce").withTag("2024.7"));
		container.start();
		container.followOutput(new Slf4jLogConsumer(log));

		return Map.of(
			"test.mqtt.port", container.getFirstMappedPort().toString(),
			"esthesis.dfl.mqtt-broker-cluster-url",
			"tcp://localhost:" + container.getFirstMappedPort().toString()
		);
	}

	@Override
	public void stop() {
		container.stop();
	}
}
