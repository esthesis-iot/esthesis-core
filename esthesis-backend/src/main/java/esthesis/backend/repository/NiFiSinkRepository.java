package esthesis.backend.repository;

import esthesis.backend.model.NiFiSink;
import org.springframework.stereotype.Repository;

@Repository
public interface NiFiSinkRepository extends BaseRepository<NiFiSink> {

  NiFiSink findByName(String name);

}
