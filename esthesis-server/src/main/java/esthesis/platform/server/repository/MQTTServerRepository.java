package esthesis.platform.server.repository;

import esthesis.platform.server.model.MqttServer;
import esthesis.platform.server.model.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MQTTServerRepository extends BaseRepository<MqttServer> {
  List<MqttServer> findAllByState(boolean state);
  List<MqttServer> findAllByTagsIn(Iterable<Tag> tags);
}
