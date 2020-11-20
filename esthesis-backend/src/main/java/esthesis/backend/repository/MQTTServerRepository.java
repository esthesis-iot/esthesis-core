package esthesis.backend.repository;

import esthesis.backend.model.MqttServer;
import esthesis.backend.model.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MQTTServerRepository extends BaseRepository<MqttServer> {
  List<MqttServer> findAllByState(boolean state);
  List<MqttServer> findAllByTagsIn(Iterable<Tag> tags);
}
