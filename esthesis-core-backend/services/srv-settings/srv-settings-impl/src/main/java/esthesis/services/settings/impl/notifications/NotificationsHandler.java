package esthesis.services.settings.impl.notifications;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.SMALLRYE_KAFKA_UNICAST_CHANNEL_IN;

import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.services.settings.impl.service.SettingsService;
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

/**
 * Handles notifications from Kafka.
 */
@Slf4j
@ApplicationScoped
public class NotificationsHandler {

	@Inject
	SettingsService settingsService;

	@Blocking
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
				case CA -> {
					switch (msg.getPayload().getAction()) {
						case DELETE -> handleCaDeleted(msg.getPayload().getTargetId());
					}
				}
			}
		} catch (Exception e) {
			log.warn("Could not handle Kafka message '{}'.", msg, e);
		} finally {
			scope.close();
		}

		return msg.ack();
	}

	/**
	 * Check if the deleted CA is used as the root CA. If so, set the platform CA to null.
	 *
	 * @param caId the CA id that was deleted.
	 */
	private void handleCaDeleted(String caId) {
		SettingEntity deviceRootCa = settingsService.findByName(NamedSetting.DEVICE_ROOT_CA);
		if (deviceRootCa != null && deviceRootCa.getValue().equals(caId)) {
			deviceRootCa.setValue(null);
			settingsService.saveUpdate(deviceRootCa);
			log.debug("Setting root CA to null.");
		}
	}

}
