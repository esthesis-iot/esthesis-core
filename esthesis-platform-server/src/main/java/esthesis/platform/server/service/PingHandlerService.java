package esthesis.platform.server.service;

import esthesis.platform.server.events.PingEvent;
import esthesis.platform.server.repository.DeviceRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Transactional
public class PingHandlerService {
  private final DeviceRepository deviceRepository;
  private final MQTTMonitor mqttMonitor;

  public PingHandlerService(DeviceRepository deviceRepository,
    MQTTMonitor mqttMonitor) {
    this.deviceRepository = deviceRepository;
    this.mqttMonitor = mqttMonitor;
  }

  @EventListener
  public void onApplicationEvent(PingEvent event) {
    mqttMonitor.setLastMessageReceived(Instant.now());
    deviceRepository.updateLastSeen(Instant.now(), event.getHardwareId());
  }
}
