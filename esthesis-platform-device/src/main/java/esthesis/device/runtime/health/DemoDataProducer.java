package esthesis.device.runtime.health;

import esthesis.device.runtime.config.AppConstants.Mqtt.EventType;
import esthesis.device.runtime.config.AppProperties;
import esthesis.device.runtime.mqtt.MqttClient;
import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.logging.Level;

@Log
@Component
public class DemoDataProducer {

  private final AppProperties appProperties;
  private final MqttClient mqttClient;

  public DemoDataProducer(AppProperties appProperties,
    MqttClient mqttClient) {
    this.appProperties = appProperties;
    this.mqttClient = mqttClient;
  }

  private String getDemoPayload() {
    String payload = appProperties.getDemoPayload();

    while (payload.matches(".*%i%.*")) {
      payload = payload.replaceFirst("%i%", String.valueOf(new Random().nextInt(100)));
    }
    while (payload.matches(".*%f%.*")) {
      payload = payload.replaceFirst("%f%", String.valueOf(new Random().nextFloat() * 100));
    }

    return payload;
  }

  @Scheduled(fixedRateString = "${demoFreqMsec:5000}", initialDelayString = "${demoInitialDelayMsec:5000}")
  public void demoScheduler() {
    if (appProperties.isDemo()) {
      String demoPayload = getDemoPayload();
      log.log(Level.FINEST, "Publishing demo payload: {0}", demoPayload);
      mqttClient.publish(EventType.TELEMETRY, demoPayload.getBytes(StandardCharsets.UTF_8));
    }
  }
}
