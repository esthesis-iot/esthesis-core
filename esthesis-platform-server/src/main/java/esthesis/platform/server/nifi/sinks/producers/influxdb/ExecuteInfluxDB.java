package esthesis.platform.server.nifi.sinks.producers.influxdb;

import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.sinks.producers.NiFiProducerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class ExecuteInfluxDB implements NiFiProducerFactory {

  private final static String NAME = "ExecuteInfluxDB";
  private final NiFiClientService niFiClientService;
  private ExecuteInfluxDBConfiguration conf;

  @Override
  public boolean supportsTelemetryProduce() {
    return true;
  }

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public String getConfigurationTemplate() {
    return "databaseName: \n"
      + "databaseUrl: \n"
      + "maxConnectionTimeoutSeconds: \n"
      + "queryResultTimeUnit: \n"
      + "query: \n"
      + "queryChunkSize: ";
  }

  @Override
  public NiFiSinkDTO createSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {

    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String executeInfluxDB = niFiClientService
      .createExecuteInfluxDB(niFiSinkDTO.getName(), conf.getDatabaseName(),
        conf.getDatabaseUrl(), Integer.parseInt(conf.getMaxConnectionTimeoutSeconds()),
        conf.getQueryResultTimeUnit()
        , conf.getQuery(), Integer.parseInt(conf.getQueryChunkSize()), path);

    niFiSinkDTO.setProcessorId(executeInfluxDB);

    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException {
    conf = extractConfiguration(sinkDTO.getConfiguration());
    return niFiClientService
      .updateExecuteInfluxDB(sinkDTO.getProcessorId(), conf.getDatabaseName(),
        conf.getDatabaseUrl(), Integer.parseInt(conf.getMaxConnectionTimeoutSeconds()),
        conf.getQueryResultTimeUnit()
        , conf.getQuery(), Integer.parseInt(conf.getQueryChunkSize()));
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

  @Override
  public boolean isSynced(NiFiSinkDTO niFiSinkDTO) {
    conf = extractConfiguration(niFiSinkDTO.getConfiguration());
    return niFiClientService.isExecuteInfluxSynced(niFiSinkDTO.getProcessorId(),
      conf.getDatabaseName(),
      conf.getDatabaseUrl(), Integer.parseInt(conf.getMaxConnectionTimeoutSeconds()),
      conf.getQueryResultTimeUnit()
      , conf.getQuery(), Integer.parseInt(conf.getQueryChunkSize()));

  }

  private ExecuteInfluxDBConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(ExecuteInfluxDBConfiguration.class),
      representer)
      .load(configuration);
  }
}
