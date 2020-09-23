package esthesis.device.runtime.proxy.web;

import esthesis.device.runtime.mqtt.MqttClient;
import esthesis.device.runtime.util.DeviceMessageUtil;
import javax.validation.constraints.NotNull;
import lombok.extern.java.Log;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@Log
@Validated
@RestController
@RequestMapping("/")
public class WebProxyServer {

  private final MqttClient mqttProxyClient;
  private final DeviceMessageUtil deviceMessageUtil;

  public WebProxyServer(MqttClient mqttProxyClient,
    DeviceMessageUtil deviceMessageUtil) {
    this.mqttProxyClient = mqttProxyClient;
    this.deviceMessageUtil = deviceMessageUtil;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> proxy(@RequestParam String topic,
    @NotNull @RequestBody String payload) {

    log.log(Level.FINEST, "Proxying to MQTT topic {0}: {1}", new String[]{topic, payload});
    mqttProxyClient
      .publish(deviceMessageUtil.resolveTopic(topic), payload.getBytes(StandardCharsets.UTF_8));

    return ResponseEntity.ok().build();
  }
}
