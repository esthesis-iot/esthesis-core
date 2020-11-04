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

//  //TODO Switch to QueryDSL?
//  @Modifying
//  @Query("update Device d set d.lastSeen = :lastSeen where d.hardwareId = :hardwareId")
//  int updateLastSeen(@Param("lastSeen") Instant lastSeen, @Param("hardwareId") String hardwareId);

  List<Device> findByHardwareIdContains(String hardwareId);

  List<Device> findByTagsIn(List<Tag> tags);

  List<Device> findAllByCreatedOnAfter(Instant date);
}
