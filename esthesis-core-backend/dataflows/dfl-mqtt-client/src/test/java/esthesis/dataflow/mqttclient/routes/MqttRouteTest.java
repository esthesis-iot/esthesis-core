package esthesis.dataflow.mqttclient.routes;

import esthesis.dataflow.mqttclient.testcontainers.HiveMQTC;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for MqttRoute, testing MQTT route configuration and functionality.
 */
@QuarkusTest
@QuarkusTestResource(value = HiveMQTC.class, restrictToAnnotatedClass = true)
class MqttRouteTest {

	@Inject
	MqttRoute mqttRoute;

	@Inject
	CamelContext context;


	@Test
	void test() {
		assertNotNull(mqttRoute);

	}

	@Test
	void shouldContainExpectedRoutes() {
		List<String> routeIds = List.of(
			"mqtt-telemetry-to-kafka",
			"mqtt-metadata-to-kafka",
			"mqtt-ping-to-kafka",
			"mqtt-command-reply-to-kafka"
		);

		for (String routeId : routeIds) {
			Route route = context.getRoute(routeId);
			assertNotNull(route, "Route with ID '" + routeId + "' should exist");
		}
	}


}
