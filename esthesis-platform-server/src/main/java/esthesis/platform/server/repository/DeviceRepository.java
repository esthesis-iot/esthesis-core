package esthesis.platform.server.repository;

import esthesis.platform.server.model.Device;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends BaseRepository<Device> {
  Optional<Device> findByHardwareId(String hardwareId);
}
