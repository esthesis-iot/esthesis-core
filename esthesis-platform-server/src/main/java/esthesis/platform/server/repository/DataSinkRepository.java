package esthesis.platform.server.repository;

import esthesis.platform.server.model.DataSink;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSinkRepository extends BaseRepository<DataSink> {

  List<DataSink> findAllByStateAndMetadataRead(boolean state, boolean metadata);

  List<DataSink> findAllByStateAndTelemetryRead(boolean state, boolean telemetry);

  List<DataSink> findAllByStateAndMetadataWrite(boolean state, boolean metadata);

  List<DataSink> findAllByStateAndTelemetryWrite(boolean state, boolean telemetry);

}
