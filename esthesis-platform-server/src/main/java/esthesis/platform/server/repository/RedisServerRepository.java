package esthesis.platform.server.repository;

import esthesis.platform.server.model.RedisServer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedisServerRepository extends BaseRepository<RedisServer> {
  List<RedisServer> findAllByState(int state);
}
