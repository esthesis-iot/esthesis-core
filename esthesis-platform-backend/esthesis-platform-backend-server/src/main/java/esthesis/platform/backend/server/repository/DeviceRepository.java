package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.Device;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends BaseRepository<Device> {
  Optional<Device> findByHardwareId(String hardwareId);
  List<Device> findByHardwareIdContains(String hardwareId);
  List<Device> findByTagsIdIn(List<Long> tags);
  List<Device> findAllByCreatedOnAfter(Instant date);
}
