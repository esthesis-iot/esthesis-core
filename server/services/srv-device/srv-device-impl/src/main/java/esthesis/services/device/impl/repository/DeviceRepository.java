package esthesis.services.device.impl.repository;

import esthesis.service.device.dto.Device;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeviceRepository implements PanacheMongoRepository<Device> {

  public Optional<Device> findByHardwareId(String hardwareId) {
    return find("hardwareId", hardwareId).firstResultOptional();
  }

  public List<Device> findByHardwareId(List<String> hardwareIds) {
    return find("hardwareId in ?1", hardwareIds).list();
  }

  public Optional<Device> findByHardwareIdPartial(String hardwareId) {
    return find("hardwareId like ?1", hardwareId).firstResultOptional();
  }

  public List<Device> findByHardwareIdPartial(List<String> hardwareIds) {
    return find("hardwareId like ?1", String.join("|", hardwareIds)).list();
  }

  public long countByHardwareId(List<String> hardwareIds) {
    return count("hardwareId in ?1", hardwareIds);
  }

  public long countByHardwareIdPartial(List<String> hardwareIds) {
    return count("hardwareId like ?1", String.join("|", hardwareIds));
  }

  public List<Device> findByTag(List<String> tags) {
    return find("tags in ?1", tags).list();
  }

  /**
   * Counts the number of devices in the specific list of tags IDs.
   *
   * @param tags The IDs of the tags to search by.
   */
  public Long countByTag(List<String> tags) {
    return count("tags in ?1", tags);
  }

}
