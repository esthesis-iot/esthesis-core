package esthesis.platform.backend.server.nifi.sinks.writers.influxdb;

import esthesis.platform.backend.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.backend.server.model.NiFiSink;
import esthesis.platform.backend.server.nifi.client.services.NiFiClientService;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.Type;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.CONSISTENCY_LEVEL;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.DATA_UNIT;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.backend.server.nifi.sinks.writers.NiFiWriterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class PutInfluxDB implements NiFiWriterFactory {

  private static final  String NAME = "PutInfluxDB";
  private final NiFiClientService niFiClientService;
  private PutInfluxDBConfiguration conf;

  @Override
  public String getFriendlyName() {
    return NAME;
  }

  @Override
  public boolean supportsMetadataWrite() {
    return true;
  }

  @Override
  public boolean supportsTelemetryWrite() {
    return true;
  }

  @Override
  public boolean supportsPingWrite() {
    return false;
  }

  @Override
  public boolean supportsCommandWrite() {
    return false;
  }

  @Override
  public String getConfigurationTemplate() {
    return
      "username: \n" +
        "password: \n" +
        "databaseName: \n" +
        "databaseUrl: \n" +
        "retentionPolicy: \n" +
        "maxConnectionTimeoutSeconds: \n" +
        "consistencyLevel: \n" +
        "charset : \n" +
        "maxRecordSize: \n" +
        "maxRecordSizeUnit:  \n" +
        "schedulingPeriod: ";
  }

  @Override
  public void createSink(NiFiSinkDTO niFiSinkDTO, String[] path)
    throws IOException {
    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    niFiClientService.createPutInfluxDB(niFiSinkDTO.getName(),
      conf.getDatabaseName(),
      conf.getDatabaseUrl(), 10,
      conf.getUsername(), conf.getPassword(),
      conf.getCharset(),
      getConsistencyLevel(conf.getConsistencyLevel()),
      conf.getRetentionPolicy(),
      getMaxRecordSize(conf.getMaxRecordSize()),
      getDataUnit(conf.getMaxRecordSizeUnit()), conf.getSchedulingPeriod(), path);

    manageConnectionWithClearQueueProcessor(path);
  }

  @Override
  public void updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO, String[] path) throws IOException {
    conf = extractConfiguration(sinkDTO.getConfiguration());

    String processorId = niFiClientService.findProcessorIDByNameAndProcessGroup(sink.getName(),
      path);

    niFiClientService.updatePutInfluxDB(processorId,
      sinkDTO.getName(), conf.getDatabaseName(),
      conf.getDatabaseUrl(), 10,
      conf.getUsername(), conf.getPassword(),
      conf.getCharset(),
      getConsistencyLevel(conf.getConsistencyLevel()),
      conf.getRetentionPolicy(),
      getMaxRecordSize(conf.getMaxRecordSize()),
      getDataUnit(conf.getMaxRecordSizeUnit()), conf.getSchedulingPeriod());
  }

  @Override
  public void deleteSink(NiFiSinkDTO niFiSinkDTO, String[] path) throws IOException {
    niFiClientService.deleteProcessor(niFiSinkDTO.getName(), path);
    manageConnectionWithClearQueueProcessor(path);
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

  @Override
  public void manageConnectionWithClearQueueProcessor(String[] path) throws IOException {
    niFiClientService.manageQueueHandling(path, Type.PUT_INFLUX_DB);
  }

  private PutInfluxDBConfiguration extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    return new Yaml(new Constructor(PutInfluxDBConfiguration.class), representer)
      .load(configuration);
  }

  private CONSISTENCY_LEVEL getConsistencyLevel(String consistencyLevel) {
    return consistencyLevel != null ? CONSISTENCY_LEVEL.valueOf(consistencyLevel.toUpperCase()) :
      CONSISTENCY_LEVEL.ONE;
  }

  private int getMaxRecordSize(String maxRecordSize) {
    return maxRecordSize != null ? Integer.parseInt(maxRecordSize) : 1;
  }

  private DATA_UNIT getDataUnit(String maxRecordSizeUnit) {
    return maxRecordSizeUnit != null ? DATA_UNIT.valueOf(maxRecordSizeUnit.toUpperCase())
      : DATA_UNIT.MB;
  }
}
