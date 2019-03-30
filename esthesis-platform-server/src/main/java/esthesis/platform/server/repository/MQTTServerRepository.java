package esthesis.platform.server.repository;

import esthesis.platform.server.model.MqttServer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MQTTServerRepository extends BaseRepository<MqttServer> {
  List<MqttServer> findAllByState(int state);
}
