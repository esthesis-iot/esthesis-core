package esthesis.platform.server.nifi.sinks.writers.influxdb;

import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NifiConstants.PATH;
import esthesis.platform.server.nifi.client.util.NifiConstants.PORTS;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.CONSISTENCY_LEVEL;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.DATA_UNIT;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.STATE;
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

  private final NiFiClientService niFiClientService;
  private final static String NAME = "PutInfluxDB";
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
        "maxRecordSizeUnit: ";
  }

  @Override
  public NiFiSinkDTO createSink(NiFiSinkDTO niFiSinkDTO)
    throws IOException {
    extractConfiguration(niFiSinkDTO.getConfiguration());
    int handler = niFiSinkDTO.getHandler();

    String putInfluxDB = niFiClientService.createPutInfluxDB(niFiSinkDTO.getName(),
      conf.getDatabaseName(),
      conf.getDatabaseUrl(), 10,
      conf.getUsername(), conf.getPassword(),
      conf.getCharset(),
      getConsistencyLevel(conf.getConsistencyLevel()),
      conf.getRetentionPolicy(),
      getMaxRecordSize(conf.getMaxRecordSize()),
      getDataUnit(conf.getMaxRecordSizeUnit()),
      findPathByHandler(handler), findInputPortByHandler(handler),
      findOutputPortByHandler(handler));

    niFiSinkDTO.setProcessorId(putInfluxDB);
    return niFiSinkDTO;
  }

  @Override
  public String updateSink(NiFiSink sink, NiFiSinkDTO sinkDTO) throws IOException {
    extractConfiguration(sinkDTO.getConfiguration());
    return niFiClientService.updatePutInfluxDB(sinkDTO.getProcessorId(), conf.getDatabaseName(),
      conf.getDatabaseUrl(), 10,
      conf.getUsername(), conf.getPassword(),
      conf.getCharset(),
      getConsistencyLevel(conf.getConsistencyLevel()),
      conf.getRetentionPolicy(),
      getMaxRecordSize(conf.getMaxRecordSize()),
      getDataUnit(conf.getMaxRecordSizeUnit()));
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
    return handler == 2 ?
      PATH.CONSUMERS_METADATA_WRITER_INFLUXDB :
      PATH.CONSUMERS_TELEMETRY_WRITER_INFLUXDB;
  }

  @Override
  public String findOutputPortByHandler(int handler) {
    return handler == 2 ?
      PORTS.CONSUMERS_METADATA_INFLUX_WRITERS_LOGOUT :
      PORTS.CONSUMERS_TELEMETRY_INFLUX_WRITERS_LOGOUT;
  }

  @Override
  public String findInputPortByHandler(int handler) {
    return handler == 2 ?
      PORTS.CONSUMERS_METADATA_INFLUX_WRITERS_IN :
      PORTS.CONSUMERS_TELEMETRY_INFLUX_WRITERS_IN;
  }

  @Override
  public String getSinkValidationErrors(String id) throws IOException {
    return niFiClientService.getValidationErrors(id);
  }

  private void extractConfiguration(String configuration) {
    Representer representer = new Representer();
    representer.getPropertyUtils().setSkipMissingProperties(true);
    conf = new Yaml(new Constructor(PutInfluxDBConfiguration.class), representer)
      .load(configuration);
  }

  private CONSISTENCY_LEVEL getConsistencyLevel(String consistencyLevel) {
    return consistencyLevel != null ? CONSISTENCY_LEVEL.valueOf(consistencyLevel.toUpperCase()) :
      CONSISTENCY_LEVEL.ONE;
  }

  private int getMaxRecordSize(String maxRecordSize) {
    return maxRecordSize != null ? Integer.getInteger(maxRecordSize) : 1;
  }

  private DATA_UNIT getDataUnit(String maxRecordSizeUnit) {
    return maxRecordSizeUnit != null ? DATA_UNIT.valueOf(maxRecordSizeUnit.toUpperCase())
      : DATA_UNIT.MB;
  }
}
