package esthesis.platform.server.nifi.sinks.loggers.filesystem;

import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.sinks.loggers.NiFiLoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class PutFile implements NiFiLoggerFactory {

  private final NiFiClientService niFiClientService;
  private PutFileConfiguration conf;

  @Override
  public boolean supportsSyslogLog() {
    return false;
  }

  @Override
  public boolean supportsFilesystemLog() {
    return true;
  }

  @Override
  public String getFriendlyName() {
    return "PutFile";
  }

  @Override
  public String getConfigurationTemplate() {
    return "directory: \n" +
      "schedulingPeriod: ";
  }

  @Override
  public NiFiSinkDTO createSink(
    NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {

    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String putFileId = niFiClientService
      .createPutFile(niFiSinkDTO.getName(), conf.getDirectory(), conf.getSchedulingPeriod(), path);

    niFiSinkDTO.setProcessorId(putFileId);

    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink,
    NiFiSinkDTO sinkDTO) throws IOException {
    conf = extractConfiguration(sinkDTO.getConfiguration());
    return niFiClientService
      .updatePutFile(sinkDTO.getProcessorId(), sinkDTO.getName(), conf.getDirectory(),
        conf.getSchedulingPeriod());
  }

  @Override
  public String deleteSink(NiFiSinkDTO niFiSinkDTO) throws IOException {
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

  private PutFileConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(PutFileConfiguration.class),
      representer)
      .load(configuration);
  }
}
