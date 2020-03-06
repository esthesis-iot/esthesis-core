package esthesis.platform.server.service;

import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_MQTT;

import esthesis.platform.server.events.LocalEvent;
import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;

/**
 * Temporary monitor class - to be removed once migrating to NiFi.
 */
@Log
@Component
public class MQTTMonitor {

  @Setter
  private Instant lastMessageReceived;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final MQTTService mqttService;

  public MQTTMonitor(ApplicationEventPublisher applicationEventPublisher,
    MQTTService mqttService) {
    this.applicationEventPublisher = applicationEventPublisher;
    this.mqttService = mqttService;
  }

  @Scheduled(initialDelay = 300000, fixedRate = 300000)
  public void checkLastMessage() {
    if (mqttService.findAll().size() > 0) {
      log.log(Level.FINE, "Last MQTT message received: {0}", lastMessageReceived);
      if (lastMessageReceived != null && lastMessageReceived.plus(15, ChronoUnit.MINUTES)
        .isBefore(Instant.now())) {
        log.log(Level.WARNING, "Haven't received MQTT messages. Restarting MQTT clients.");
        applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_MQTT));
      }
    }
  }
}
