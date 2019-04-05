package esthesis.platform.server.repository;

import esthesis.platform.server.model.ZookeeperServer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZookeeperServerRepository extends BaseRepository<ZookeeperServer> {
  List<ZookeeperServer> findAllByState(boolean state);
}
