package esthesis.platform.server.repository;

import esthesis.platform.server.model.Device;
import esthesis.platform.server.model.Tag;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends BaseRepository<Device> {
  Optional<Device> findByHardwareId(String hardwareId);

  List<Device> findByHardwareIdContains(String hardwareId);

  List<Device> findByTagsIn(List<Tag> tags);

  List<Device> findAllByCreatedOnAfter(Instant date);
}
