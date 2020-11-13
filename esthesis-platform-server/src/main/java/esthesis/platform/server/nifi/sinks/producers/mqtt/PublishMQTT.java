package esthesis.platform.server.nifi.sinks.producers.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.dto.NiFiSearchAlgorithm;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.SUCCESSFUL_RELATIONSHIP_TYPES;
import esthesis.platform.server.nifi.sinks.producers.NiFiProducerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class PublishMQTT implements NiFiProducerFactory {

  private static final String NAME = "PublishMQTT";
  private final ObjectMapper objectMapper;
  private final NiFiClientService niFiClientService;
  private PublishMQTTConfiguration conf;

  @Override
  public boolean supportsTelemetryProduce() {
    return false;
  }

  @Override
  public boolean supportsMetadataProduce() {
    return false;
  }

  @Override
  public boolean supportsCommandProduce() {
    return true;
  }

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public String getConfigurationTemplate() {
    return
      "uri: \n" +
        "topic: \n" +
        "qos: \n" +
        "retainMesage: \n" +
        "keystoreFilename: \n" +
        "keystorePassword: \n" +
        "truststoreFilename: \n" +
        "truststorePassword:  \n" +
        "schedulingPeriod: ";
  }

  @Override
  public void createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    deleteControllerServices(niFiSinkDTO);
    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String sslContextId = null;
    String keystoreFilename = conf.getKeystoreFilename();
    String keystorePassword = conf.getKeystorePassword();
    String truststoreFilename = conf.getTruststoreFilename();
    String truststorePassword = conf.getTruststorePassword();

    if (ObjectUtils
      .allNotNull(keystoreFilename, keystorePassword, truststoreFilename, truststorePassword)) {

      sslContextId = niFiClientService.createSSLContext(niFiSinkDTO.getName() + " [SSL Context] ",
        keystoreFilename, keystorePassword, truststoreFilename, truststorePassword, path);

      CustomInfo customInfo = new CustomInfo();
      customInfo.setSslContextId(sslContextId);
      niFiSinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));
      enableControllerServices(sslContextId);
    }

    String mqttPublisherId = niFiClientService
      .createMQTTPublisher(niFiSinkDTO.getName(), conf.getUri(), conf.getTopic(), conf.getQos(),
        conf.isRetainMessage(), sslContextId, conf.getSchedulingPeriod(), path,
        true);

    Set<String> relationship = new HashSet<>(
      Arrays.asList(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType()));


    String mqttCommandBodyGeneratorId = niFiClientService.findProcessorIDByNameAndProcessGroup(
      "[GMCB]", path,
      NiFiSearchAlgorithm.NAME_ENDS_WITH);
    String commandIdResponseSetterId = niFiClientService.findProcessorIDByNameAndProcessGroup("[SCIDR]", path,
      NiFiSearchAlgorithm.NAME_ENDS_WITH);

    niFiClientService.connectComponentsInSameGroup(path, mqttCommandBodyGeneratorId,
      mqttPublisherId, relationship);
    niFiClientService.connectComponentsInSameGroup(path, mqttPublisherId, commandIdResponseSetterId, relationship);
    niFiClientService.moveComponent(path, mqttPublisherId);
    niFiClientService.changeProcessorGroupState(path, STATE.RUNNING);

    if (!niFiSinkDTO.isState()) {
      niFiClientService.changeProcessorStatus(niFiSinkDTO.getName(), path, STATE.STOPPED);
    }
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO, String[] path) throws IOException {
    PublishMQTTConfiguration prevConf = extractConfiguration(sink.getConfiguration());
    conf = extractConfiguration(sinkDTO.getConfiguration());
    String sslContextId = null;
    String processorId = niFiClientService.findProcessorIDByNameAndProcessGroup(sink.getName(),
      path);

    if (!(Objects.equals(conf.getKeystoreFilename(), prevConf.getKeystoreFilename()) &&
      Objects.equals(conf.getKeystorePassword(), prevConf.getKeystorePassword()) &&
      Objects.equals(conf.getTruststoreFilename(), prevConf.getTruststoreFilename()) &&
      Objects.equals(conf.getTruststorePassword(), prevConf.getTruststorePassword()))) {

      CustomInfo customInfo =
        sinkDTO.getCustomInfo() != null ?
          objectMapper.readValue(sink.getCustomInfo(),
           CustomInfo.class) : null;

      if (customInfo == null) {
        sslContextId = niFiClientService
          .createSSLContextForExistingProcessor(sinkDTO.getName(), path,
            conf.getKeystoreFilename(), conf.getKeystorePassword(), conf.getTruststoreFilename(),
            conf.getTruststorePassword());

        customInfo = new CustomInfo();
        customInfo.setSslContextId(sslContextId);
        sinkDTO.setCustomInfo(objectMapper.writeValueAsString(customInfo));
        enableControllerServices(sslContextId);
      } else {
        niFiClientService
          .updateSSLContext(customInfo.getSslContextId(), conf.getKeystoreFilename(),
            conf.getKeystorePassword(), conf.getTruststoreFilename(), conf.getTruststorePassword());
      }
    }

    return niFiClientService
      .updatePublisherMQTT(processorId, sinkDTO.getName(), sslContextId, conf.getUri(),
        conf.getTopic(),
        conf.getQos(), conf.isRetainMessage(), conf.getSchedulingPeriod());
  }

  @Override
  public void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    niFiClientService.changeProcessorGroupState(path, STATE.STOPPED);
    niFiClientService.deleteProcessor(niFiSinkDTO.getName(), path);
    deleteControllerServices(niFiSinkDTO);
  }

  private void deleteControllerServices(NiFiSinkDTO niFiSinkDTO) throws IOException {
    String customInfoString = niFiSinkDTO.getCustomInfo();
    if (customInfoString != null) {
      CustomInfo customInfo = objectMapper
        .readValue(customInfoString, CustomInfo.class);
      niFiClientService.deleteController(customInfo.getSslContextId());
    }
  }

  @Override
  public String toggleSink(String name, String[] path, boolean isEnabled) throws IOException {
    return niFiClientService.changeProcessorStatus(name, path,
      isEnabled ? STATE.RUNNING : STATE.STOPPED);
  }

  @Override
  public void enableControllerServices(String... controllerServices) throws IOException {
    for (String id : controllerServices) {
      niFiClientService.changeControllerServiceStatus(id, STATE.ENABLED);
    }
  }

  @Override
  public String getSinkValidationErrors(String name, String[] path) throws IOException {
    return niFiClientService.getValidationErrors(name, path);
  }

  @Override
  public boolean exists(String name, String[] path) throws IOException {
    return niFiClientService.processorExists(name, path);
  }

  @Override
  public boolean isSinkRunning(String name, String[] path) throws IOException {
    return niFiClientService.isProcessorRunning(name, path);
  }

  private PublishMQTTConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils()
      .setSkipMissingProperties(true);
    return new Yaml(new Constructor(PublishMQTTConfiguration.class),
      representer)
      .load(configuration);
  }
}
