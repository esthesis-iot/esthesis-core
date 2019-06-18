package esthesis.platform.server.repository;

import esthesis.platform.server.model.DevicePage;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceMetadataRepository extends BaseRepository<DevicePage> {
  DevicePage findByName(String name);
}
