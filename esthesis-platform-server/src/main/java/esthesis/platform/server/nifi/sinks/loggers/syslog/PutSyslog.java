package esthesis.platform.server.nifi.sinks.loggers.syslog;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.sinks.loggers.NiFiLoggerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class PutSyslog implements NiFiLoggerFactory {

  private final NiFiClientService niFiClientService;
  private final ObjectMapper objectMapper;
  private PutSyslogConfiguration conf;

  @Override
  public boolean supportsSyslogLog() {
    return true;
  }

  @Override
  public boolean supportsFilesystemLog() {
    return false;
  }

  @Override
  public String getFriendlyName() {
    return "PutSyslog";
  }

  @Override
  public String getConfigurationTemplate() {
    return "hostname: \n" +
      "port: \n" +
      "protocol: \n" +
      "messagePriority: \n" +
      "messageBody: \n" +
      "keystoreFilename: \n" +
      "keystorePassword: \n" +
      "truststoreFilename: \n" +
      "truststorePassword:  \n" +
      "schedulingPeriod: ";
  }

  @Override
  public NiFiSinkDTO createSink(
    NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {

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

    String putSyslog = niFiClientService
      .createPutSyslog(niFiSinkDTO.getName(), sslContextId, conf.getHostname(),
        conf.getPort(),
        conf.getProtocol(), conf.getMessageBody(), conf.getMessagePriority(), conf.getSchedulingPeriod(), path);

    niFiSinkDTO.setProcessorId(putSyslog);

    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink,
    NiFiSinkDTO sinkDTO) throws IOException {

    PutSyslogConfiguration prevConf = extractConfiguration(sink.getConfiguration());
    conf = extractConfiguration(sinkDTO.getConfiguration());

    String sslContextId = null;

    if (!(Objects.equals(conf.getKeystoreFilename(), prevConf.getKeystoreFilename()) &&
      Objects.equals(conf.getKeystorePassword(), prevConf.getKeystorePassword()) &&
      Objects.equals(conf.getTruststoreFilename(), prevConf.getTruststoreFilename()) &&
      Objects.equals(conf.getTruststorePassword(), prevConf.getTruststorePassword()))) {

      esthesis.platform.server.nifi.sinks.readers.mqtt.CustomInfo customInfo =
        sinkDTO.getCustomInfo() != null ?
          objectMapper.readValue(sink.getCustomInfo(),
            esthesis.platform.server.nifi.sinks.readers.mqtt.CustomInfo.class) : null;

      if (customInfo == null) {

        sslContextId = niFiClientService
          .createSSLContextForExistingProcessor(sinkDTO.getProcessorId(),
            conf.getKeystoreFilename(),
            conf.getKeystorePassword(), conf.getTruststoreFilename(), conf.getTruststorePassword());

        customInfo = new esthesis.platform.server.nifi.sinks.readers.mqtt.CustomInfo();
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
      .updatePutSyslog(sink.getProcessorId(),sinkDTO.getName(), sslContextId, conf.getHostname(),
        conf.getPort(),
        conf.getProtocol(), conf.getMessageBody(), conf.getMessagePriority(), conf.getSchedulingPeriod());
  }

  @Override
  public String deleteSink(NiFiSinkDTO niFiSinkDTO) throws IOException {
    String customInfoString = niFiSinkDTO.getCustomInfo();
    if (customInfoString != null) {
      CustomInfo customInfo = objectMapper.readValue(customInfoString, CustomInfo.class);
      niFiClientService.deleteController(customInfo.getSslContextId());
    }
    return niFiClientService.deleteProcessor(niFiSinkDTO.getProcessorId());
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

  @Override
  public boolean exists(String id) throws IOException {
    return niFiClientService.processorExists(id);
  }

  @Override
  public boolean isSinkRunning(String id) throws IOException {
    return niFiClientService.isProcessorRunning(id);
  }

  private PutSyslogConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(PutSyslogConfiguration.class),
      representer)
      .load(configuration);
  }

}
