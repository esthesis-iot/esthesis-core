package esthesis.platform.server.nifi.sinks.writers.influxdb;

import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.CONSISTENCY_LEVEL;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.DATA_UNIT;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.sinks.writers.NiFiWriterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class PutInfluxDB implements NiFiWriterFactory {

  private final static String NAME = "PutInfluxDB";
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
  public NiFiSinkDTO createSink(NiFiSinkDTO niFiSinkDTO, String[] path)
    throws IOException {
    conf = extractConfiguration(niFiSinkDTO.getConfiguration());

    String putInfluxDB = niFiClientService.createPutInfluxDB(niFiSinkDTO.getName(),
      conf.getDatabaseName(),
      conf.getDatabaseUrl(), 10,
      conf.getUsername(), conf.getPassword(),
      conf.getCharset(),
      getConsistencyLevel(conf.getConsistencyLevel()),
      conf.getRetentionPolicy(),
      getMaxRecordSize(conf.getMaxRecordSize()),
      getDataUnit(conf.getMaxRecordSizeUnit()), conf.getSchedulingPeriod(), path);

    niFiSinkDTO.setProcessorId(putInfluxDB);
    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException {
    conf = extractConfiguration(sinkDTO.getConfiguration());
    return niFiClientService.updatePutInfluxDB(sinkDTO.getProcessorId(),
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
