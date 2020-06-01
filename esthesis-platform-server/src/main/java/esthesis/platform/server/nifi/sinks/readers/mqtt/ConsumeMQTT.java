package esthesis.platform.server.nifi.sinks.readers.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NifiConstants.PATH;
import esthesis.platform.server.nifi.client.util.NifiConstants.PORTS;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.sinks.readers.NiFiReaderFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ConsumeMQTT implements NiFiReaderFactory {

  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private final static String NAME = "ConsumeMQTT";
  private ConsumeMQTTConfiguration consumeMQTTConfiguration;

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public boolean supportsPingRead() {
    return true;
  }

  @Override
  public boolean supportsMetadataRead() {
    return true;
  }

  @Override
  public boolean supportsTelemetryRead() {
    return true;
  }

  public String getConfigurationTemplate() {
    return
      "uri: \n" +
        "topic: \n" +
        "qos: \n" +
        "queueSize: \n" +
        "keystoreFilename: \n" +
        "keystorePassword: \n" +
        "truststoreFilename: \n" +
        "truststorePassword: ";
  }

  @Override
  public NiFiSinkDTO createSink(NiFiSinkDTO niFiSinkDTO) throws IOException {

    extractConfiguration(niFiSinkDTO.getConfiguration());

    PATH pathByHandler = findPathByHandler(niFiSinkDTO.getHandler());
    String portByHandler = findOutputPortByHandler(niFiSinkDTO.getHandler());

    String sslContextId = null;
    consumeMQTTConfiguration.getKeystoreFilename();
    consumeMQTTConfiguration.getKeystoreFilename();
    consumeMQTTConfiguration.getTruststoreFilename();
    consumeMQTTConfiguration.getTruststorePassword();

    if (ObjectUtils.allNotNull(consumeMQTTConfiguration.getKeystoreFilename(),
      consumeMQTTConfiguration.getKeystoreFilename(),
      consumeMQTTConfiguration.getTruststoreFilename(),
      consumeMQTTConfiguration.getTruststorePassword())) {

      sslContextId = niFiClientService.createSSLContext(niFiSinkDTO.getName() + " [SSL Context] ",
        consumeMQTTConfiguration.getKeystoreFilename(),
        consumeMQTTConfiguration.getKeystorePassword(),
        consumeMQTTConfiguration.getTruststoreFilename(),
        consumeMQTTConfiguration.getTruststorePassword(), pathByHandler);

      CustomInfo customInfo = new CustomInfo();
      customInfo.setSslContextId(sslContextId);
      niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));
    }

    String consumerMqtt = niFiClientService
      .createConsumerMqtt(niFiSinkDTO.getName(), consumeMQTTConfiguration.getUri(),
        consumeMQTTConfiguration.getTopic(), consumeMQTTConfiguration.getQos(),
        consumeMQTTConfiguration.getQueueSize(), sslContextId, pathByHandler, portByHandler);

    niFiSinkDTO.setProcessorId(consumerMqtt);
    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException {
    extractConfiguration(sinkDTO.getConfiguration());
    return niFiClientService
      .updateConsumerMQTT(sinkDTO.getProcessorId(), consumeMQTTConfiguration.getUri(),
        consumeMQTTConfiguration.getTopic(), consumeMQTTConfiguration.getQos(),
        consumeMQTTConfiguration.getQueueSize());
  }

  @Override
  public String deleteSink(String id) throws IOException {
    return niFiClientService.deleteProcessor(id);
  }

  @Override
  public String toggleSink(String id, boolean isEnabled) throws IOException {
    return niFiClientService.changeProcessorStatus(id, isEnabled ? STATE.RUNNING : STATE.STOPPED);
  }

  @Override
  public void enableControllerServices(String... controllerServices) throws IOException {
    for (String id : controllerServices) {
      niFiClientService.changeControllerServiceStatus(id, STATE.ENABLED);
    }
  }

  @Override
  public PATH findPathByHandler(int handler) {
    switch (handler) {
      case 1:
        return PATH.CONSUMERS_PING_CONSUMER_MQTT;
      case 2:
        return PATH.CONSUMERS_METADATA_CONSUMER_MQTT;
      default:
        return PATH.CONSUMERS_TELEMETRY_CONSUMER_MQTT;
    }
  }

  @Override
  public String findOutputPortByHandler(int handler) {
    switch (handler) {
      case 1:
        return PORTS.CONSUMERS_PING_MQTT_CONSUMERS_OUT;
      case 2:
        return PORTS.CONSUMERS_METADATA_MQTT_CONSUMERS_OUT;
      default:
        return PORTS.CONSUMERS_TELEMETRY_MQTT_CONSUMERS_OUT;
    }
  }

  @Override
  public String getSinkValidationErrors(String id) throws IOException {
    return niFiClientService.getValidationErrors(id);
  }

  private void extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils()
      .setSkipMissingProperties(true);
    consumeMQTTConfiguration = new Yaml(new Constructor(ConsumeMQTTConfiguration.class),
      representer)
      .load(configuration);
  }
}
