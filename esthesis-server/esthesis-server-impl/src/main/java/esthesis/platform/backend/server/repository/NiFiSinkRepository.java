package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.NiFiSink;
import org.springframework.stereotype.Repository;

@Repository
public interface NiFiSinkRepository extends BaseRepository<NiFiSink> {

  NiFiSink findByName(String name);

}
