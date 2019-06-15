package esthesis.platform.server.repository;

import esthesis.platform.server.model.DeviceMetadata;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceMetadataRepository extends BaseRepository<DeviceMetadata> {
  DeviceMetadata findByName(String name);
}
