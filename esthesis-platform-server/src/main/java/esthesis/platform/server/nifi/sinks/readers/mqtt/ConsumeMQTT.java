package esthesis.platform.server.nifi.sinks.readers.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
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

  private final static String NAME = "ConsumeMQTT";
  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private ConsumeMQTTConfiguration conf;

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
  public NiFiSinkDTO createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {

    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String sslContextId = null;
    conf.getKeystoreFilename();
    conf.getKeystoreFilename();
    conf.getTruststoreFilename();
    conf.getTruststorePassword();

    if (ObjectUtils.allNotNull(conf.getKeystoreFilename(),
      conf.getKeystoreFilename(),
      conf.getTruststoreFilename(),
      conf.getTruststorePassword())) {

      sslContextId = niFiClientService.createSSLContext(niFiSinkDTO.getName() + " [SSL Context] ",
        conf.getKeystoreFilename(),
        conf.getKeystorePassword(),
        conf.getTruststoreFilename(),
        conf.getTruststorePassword(), path);

      CustomInfo customInfo = new CustomInfo();
      customInfo.setSslContextId(sslContextId);
      niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));
      enableControllerServices(sslContextId);
    }

    String consumerMqtt = niFiClientService
      .createConsumerMqtt(niFiSinkDTO.getName(), conf.getUri(),
        conf.getTopic(), conf.getQos(),
        conf.getQueueSize(), sslContextId, path);

    niFiSinkDTO.setProcessorId(consumerMqtt);
    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException {
    ConsumeMQTTConfiguration prevConf = extractConfiguration(sink.getConfiguration());
    conf = extractConfiguration(sinkDTO.getConfiguration());

    if (!(conf.getKeystoreFilename().equals(prevConf.getKeystoreFilename()) && conf
      .getKeystorePassword().equals(prevConf.getKeystorePassword()) && conf.getTruststoreFilename()
      .equals(prevConf.getTruststoreFilename()) && conf.getTruststorePassword()
      .equals(prevConf.getTruststorePassword()))) {

      CustomInfo customInfo = objectMapper.readValue(sink.getCustomInfo(), CustomInfo.class);

      niFiClientService.updateSSLContext(customInfo.getSslContextId(), conf.getKeystoreFilename(),
        conf.getKeystorePassword(), conf.getTruststoreFilename(), conf.getKeystoreFilename());
    }

    return niFiClientService
      .updateConsumerMQTT(sinkDTO.getProcessorId(), conf.getUri(),
        conf.getTopic(), conf.getQos(),
        conf.getQueueSize());
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
  public String getSinkValidationErrors(String id) throws IOException {
    return niFiClientService.getValidationErrors(id);
  }

  private ConsumeMQTTConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils()
      .setSkipMissingProperties(true);
    return new Yaml(new Constructor(ConsumeMQTTConfiguration.class),
      representer)
      .load(configuration);
  }
}
