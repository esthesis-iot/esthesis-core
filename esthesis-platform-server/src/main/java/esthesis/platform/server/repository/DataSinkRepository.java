package esthesis.platform.server.repository;

import esthesis.platform.server.model.DataSink;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSinkRepository extends BaseRepository<DataSink> {

  List<DataSink> findAllByStateIsTrueAndSinkType(String sinkType);

}
