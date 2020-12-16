package esthesis.device.runtime.proxy.web;

import esthesis.device.runtime.config.AppConstants.Mqtt.EventType;
import esthesis.device.runtime.mqtt.MqttClient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.java.Log;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Log
@Validated
@RestController
@RequestMapping("/")
public class WebProxyServer {

  private final MqttClient mqttProxyClient;

  public WebProxyServer(MqttClient mqttProxyClient) {
    this.mqttProxyClient = mqttProxyClient;
  }

  @PostMapping(path = "telemetry", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> proxyTelemetry(@NotNull @NotEmpty @RequestBody String payload) {
    mqttProxyClient.publish(EventType.TELEMETRY, payload.getBytes(StandardCharsets.UTF_8));

    return ResponseEntity.ok().build();
  }

  @PostMapping(path = "metadata", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> proxyMetadata(@NotNull @NotEmpty @RequestBody String payload) {
    mqttProxyClient.publish(EventType.METADATA, payload.getBytes(StandardCharsets.UTF_8));

    return ResponseEntity.ok().build();
  }
}
