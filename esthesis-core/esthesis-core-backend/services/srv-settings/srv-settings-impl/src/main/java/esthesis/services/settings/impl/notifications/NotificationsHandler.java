package esthesis.services.settings.impl.notifications;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.SMALLRYE_KAFKA_CHANNEL_IN;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.services.settings.impl.service.SettingsService;
import esthesis.util.kafka.notifications.common.AppMessage;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.TracingMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@ApplicationScoped
public class NotificationsHandler {

	@Inject
	SettingsService settingsService;

	@Blocking
	@Incoming(SMALLRYE_KAFKA_CHANNEL_IN)
	public CompletionStage<Void> onMessage(Message<AppMessage> msg) {
		log.trace("Processing Kafka application message '{}'", msg);
		Scope scope = Context.current().makeCurrent();

		try {
			Optional<TracingMetadata> tracingMetadata = msg.getMetadata().get(TracingMetadata.class);
			if (tracingMetadata.isPresent()) {
				scope = tracingMetadata.get().getCurrentContext().makeCurrent();
			}
			switch (msg.getPayload().getComponent()) {
				case CA -> {
					switch (msg.getPayload().getAction()) {
						case DELETE -> handleCaDeleted(msg.getPayload().getTargetId());
					}
				}
				case CERTIFICATE -> {
					switch (msg.getPayload().getAction()) {
						case DELETE -> handleCertificateDeleted(msg.getPayload().getTargetId());
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
	 * Check if the deleted certificate is used as the platform certificate. If so, set the platform
	 * certificate to null.
	 *
	 * @param certificateId the certificate id that was deleted.
	 */
	private void handleCertificateDeleted(String certificateId) {
		SettingEntity platformCertificate = settingsService.findByName(
			NamedSetting.PLATFORM_CERTIFICATE);
		if (platformCertificate != null && platformCertificate.getValue().equals(certificateId)) {
			platformCertificate.setValue(null);
			settingsService.save(platformCertificate);
			log.debug("Setting platform certificate to null.");
		}
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
			settingsService.save(deviceRootCa);
			log.debug("Setting root CA to null.");
		}
	}

}
