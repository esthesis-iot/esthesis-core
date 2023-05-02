package esthesis.dataflows.oriongateway.service;

import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.util.kafka.notifications.common.AppMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

@Slf4j
@ApplicationScoped
public class OrionMessagingService {

	@Inject
	OrionGatewayService orionGatewayService;

	@Inject
	AppConfig appConfig;

	@SuppressWarnings({"java:S1301", "java:S108", "java:S131", "java:S6205" })
	public void onAppMessage(Exchange exchange) {
		AppMessage appMessage = exchange.getIn().getBody(AppMessage.class);
		log.debug("Received message '{}'", appMessage);

		switch (appMessage.getComponent()) {
			case DEVICE -> {
				switch (appMessage.getSubject()) {
					case DEVICE -> {
						switch (appMessage.getAction()) {
							case CREATE -> {
								if (appConfig.orionCreateDevice()) {
									onCreateDeviceMessage(appMessage);
								} else {
									log.trace("Creating new devices is disabled.");
								}
							}
							case DELETE -> {
								if (appConfig.orionDeleteDevice()) {
									onDeleteDeviceMessage(appMessage);
								} else {
									log.trace("Deleting devices is disabled.");
								}
							}
						}
					}
					case DEVICE_ATTRIBUTE -> {
						switch (appMessage.getAction()) {
							case UPDATE -> {
								if (appConfig.orionUpdateData()) {
									onUpdateDeviceAtrributeMessage(appMessage);
								} else {
									log.trace("Updating device attributes is disabled.");
								}
							}
						}
					}
				}
			}
		}
	}

	public void onCreateDeviceMessage(AppMessage appMessage) {
		log.debug("Received device creation message '{}'.", appMessage);
		// Extract the device esthesis ID.
		String esthesisId = appMessage.getId();

		// Register the device in Orion.
		orionGatewayService.registerDeviceOnOrion(esthesisId);
	}

	public void onUpdateDeviceAtrributeMessage(AppMessage appMessage) {
		// Find the device in Orion.
		String esthesisId = appMessage.getId();

		// Sync attributes.
		orionGatewayService.syncAttributes(esthesisId);
	}

	public void onDeleteDeviceMessage(AppMessage appMessage) {
		log.debug("Received device removal message '{}'.", appMessage);
		// Extract the device esthesis ID.
		String esthesisId = appMessage.getId();

		// Delete the Orion entity.
		orionGatewayService.deleteEntityByEsthesisId(esthesisId);
	}
}
