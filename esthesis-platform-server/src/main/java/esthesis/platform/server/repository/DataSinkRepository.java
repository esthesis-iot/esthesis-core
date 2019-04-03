package esthesis.platform.server.repository;

import esthesis.platform.server.model.DataSink;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSinkRepository extends BaseRepository<DataSink> {

  List<DataSink> findAllByStateAndMetadata(boolean state, boolean metadata);

  List<DataSink> findAllByStateAndTelemetry(boolean state, boolean telemetry);

}
