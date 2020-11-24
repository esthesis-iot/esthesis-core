package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.MqttServer;
import esthesis.platform.backend.server.model.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MQTTServerRepository extends BaseRepository<MqttServer> {
  List<MqttServer> findAllByState(boolean state);
  List<MqttServer> findAllByTagsIn(Iterable<Tag> tags);
}
