package esthesis.device.runtime.proxy.web;

import esthesis.device.runtime.mqtt.MqttClient;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Validated
@RestController
@RequestMapping("/")
public class WebProxyServer {

  private final MqttClient mqttProxyClient;

  public WebProxyServer(MqttClient mqttProxyClient) {
    this.mqttProxyClient = mqttProxyClient;
  }

  @PostMapping(path = "{topic}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity save(@PathVariable @NotNull String topic,
    @NotNull @RequestBody String payload) {
    mqttProxyClient.publish(topic, payload.getBytes(StandardCharsets.UTF_8));

    return ResponseEntity.ok().build();
  }
}
