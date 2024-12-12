package esthesis.services.device.impl.notifications;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.SMALLRYE_KAFKA_UNICAST_CHANNEL_IN;

import esthesis.services.device.impl.service.DeviceTagService;
import esthesis.util.kafka.notifications.common.AppMessage;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.TracingMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@ApplicationScoped
public class NotificationsHandler {

	@Inject
	DeviceTagService deviceTagService;

	private void handleTagDeleted(String tagId) {
		deviceTagService.removeTagById(tagId);
	}

	@Blocking
	@SuppressWarnings("java:S1301")
	@Incoming(SMALLRYE_KAFKA_UNICAST_CHANNEL_IN)
	public CompletionStage<Void> onMessage(Message<AppMessage> msg) {
		log.trace("Processing Kafka application message '{}'", msg);
		// Set the context for the message.
		Scope scope = msg.getMetadata().get(TracingMetadata.class)
			.map(tm -> tm.getCurrentContext().makeCurrent())
			.orElse(Context.current().makeCurrent());

		// Process the message.
		try {
			switch (msg.getPayload().getComponent()) {
				case TAG -> {
					switch (msg.getPayload().getAction()) {
						case DELETE -> handleTagDeleted(msg.getPayload().getTargetId());
						default -> log.trace("Ignoring Kafka message '{}'.", msg);
					}
				}
				default -> log.trace("Ignoring Kafka message '{}'.", msg);
			}
		} catch (Exception e) {
			log.warn("Could not handle Kafka message '{}'.", msg, e);
		} finally {
			scope.close();
		}

		return msg.ack();
	}
}
