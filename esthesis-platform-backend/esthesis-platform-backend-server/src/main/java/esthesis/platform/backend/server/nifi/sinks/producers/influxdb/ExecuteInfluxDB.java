package esthesis.platform.backend.server.nifi.sinks.producers.influxdb;

import esthesis.platform.backend.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.backend.server.model.NiFiSink;
import esthesis.platform.backend.server.nifi.client.services.NiFiClientService;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.backend.server.nifi.sinks.producers.NiFiProducerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class ExecuteInfluxDB implements NiFiProducerFactory {

  private static final String NAME = "ExecuteInfluxDB";
  private final NiFiClientService niFiClientService;
  private ExecuteInfluxDBConfiguration conf;

  @Override
  public boolean supportsTelemetryProduce() {
    return true;
  }

  @Override
  public boolean supportsMetadataProduce() {
    return true;
  }

  @Override
  public boolean supportsCommandProduce() {
    return false;
  }

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public String getConfigurationTemplate() {
    return "username: \n"
      + "password: \n"
      + "databaseName: \n"
      + "databaseUrl: \n"
      + "maxConnectionTimeoutSeconds: \n"
      + "queryResultTimeUnit: \n"
      + "queryChunkSize: \n"
      + "schedulingPeriod: ";
  }

  @Override
  public void createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {

    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    niFiClientService
      .createExecuteInfluxDB(niFiSinkDTO.getName(), conf.getDatabaseName(),
        conf.getDatabaseUrl(), Integer.parseInt(conf.getMaxConnectionTimeoutSeconds()),
        conf.getUsername(), conf.getPassword(),
        conf.getQueryResultTimeUnit(), Integer.parseInt(conf.getQueryChunkSize()),
        conf.getSchedulingPeriod(),
        path);

    niFiClientService.distributeLoadOfProducers(niFiSinkDTO.getHandler(), false);
  }

  @Override
  public void updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO, String[] path) throws IOException {
    conf = extractConfiguration(sinkDTO.getConfiguration());
    String processorId = niFiClientService.findProcessorIDByNameAndProcessGroup(sink.getName(),
      path);

    niFiClientService
      .updateExecuteInfluxDB(processorId, sinkDTO.getName(), conf.getDatabaseName(),
        conf.getDatabaseUrl(), Integer.parseInt(conf.getMaxConnectionTimeoutSeconds()),
        conf.getUsername(), conf.getPassword(),
        conf.getQueryResultTimeUnit(), Integer.parseInt(conf.getQueryChunkSize()),
        conf.getSchedulingPeriod());
  }

  @Override
  public void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    niFiClientService.deleteProcessor(niFiSinkDTO.getName(), path);
    niFiClientService.distributeLoadOfProducers(niFiSinkDTO.getHandler(), false);
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

  private ExecuteInfluxDBConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(ExecuteInfluxDBConfiguration.class),
      representer)
      .load(configuration);
  }
}
