package esthesis.platform.server.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class PingHandlerService {
//  private final DeviceRepository deviceRepository;
//  private final MQTTMonitor mqttMonitor;
//
//  public PingHandlerService(DeviceRepository deviceRepository,
//    MQTTMonitor mqttMonitor) {
//    this.deviceRepository = deviceRepository;
//    this.mqttMonitor = mqttMonitor;
//  }
//
//  @EventListener
//  public void onApplicationEvent(PingEvent event) {
//    mqttMonitor.setLastMessageReceived(Instant.now());
//    deviceRepository.updateLastSeen(Instant.now(), event.getHardwareId());
//  }
}
