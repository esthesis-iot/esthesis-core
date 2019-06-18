package esthesis.platform.server.repository;

import esthesis.platform.server.model.Device;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface DeviceRepository extends BaseRepository<Device> {
  Optional<Device> findByHardwareId(String hardwareId);

  @Modifying
  @Query("update Device d set d.lastSeen = :lastSeen where d.hardwareId = :hardwareId")
  int updateLastSeen(@Param("lastSeen") Instant lastSeen, @Param("hardwareId") String hardwareId);
}
