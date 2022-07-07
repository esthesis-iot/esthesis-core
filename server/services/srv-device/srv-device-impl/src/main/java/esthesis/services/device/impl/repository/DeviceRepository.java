package esthesis.services.device.impl.repository;

import esthesis.service.device.dto.Device;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeviceRepository implements PanacheMongoRepository<Device> {

  public Optional<Device> findByHardwareId(String hardwareId) {
    return find("hardwareId", hardwareId).firstResultOptional();
  }

//  public List<Device> findByHardwareIdContainsOrderByHardwareId(String hardwareId) {
//
//  }

//  public List<Device> findByTagsIdIn(List<Long> tags) {
//
//  }

//  public List<Device> findAllByCreatedOnAfter(Instant date) {
//
//  }


}
