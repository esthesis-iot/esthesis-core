package esthesis.platform.server.repository;

import esthesis.platform.server.model.NiFiSink;
import org.springframework.stereotype.Repository;

@Repository
public interface NiFiSinkRepository extends BaseRepository<NiFiSink> {

  NiFiSink findByName(String name);

}
