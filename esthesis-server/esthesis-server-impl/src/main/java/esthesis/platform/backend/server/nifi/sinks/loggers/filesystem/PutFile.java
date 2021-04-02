package esthesis.platform.backend.server.nifi.sinks.loggers.filesystem;

import esthesis.platform.backend.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.backend.server.model.NiFiSink;
import esthesis.platform.backend.server.nifi.client.services.NiFiClientService;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.backend.server.nifi.sinks.loggers.NiFiLoggerFactory;
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
  public void createSink(
    NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {

    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    niFiClientService
      .createPutFile(niFiSinkDTO.getName(), conf.getDirectory(), conf.getSchedulingPeriod(), path);
  }

  @Override
  public void updateSink(NiFiSink sink,
    NiFiSinkDTO sinkDTO, String[] path) throws IOException {
    conf = extractConfiguration(sinkDTO.getConfiguration());

    String processorId = niFiClientService.findProcessorIDByNameAndProcessGroup(sink.getName(),
      path);

   niFiClientService
      .updatePutFile(processorId, sinkDTO.getName(), conf.getDirectory(),
        conf.getSchedulingPeriod());
  }

  @Override
  public void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    niFiClientService.deleteProcessor(niFiSinkDTO.getName(), path);
  }

  @Override
  public void toggleSink(String name, String[] path, boolean isEnabled) throws IOException {
   niFiClientService.changeProcessorStatus(name, path,
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

  private PutFileConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(PutFileConfiguration.class),
      representer)
      .load(configuration);
  }
}
