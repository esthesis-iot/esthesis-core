package esthesis.services.device.impl.repository;

import esthesis.service.device.entity.DeviceAttributeEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeviceAttributeRepository implements PanacheMongoRepository<DeviceAttributeEntity> {

  public List<DeviceAttributeEntity> findByDeviceId(String deviceId) {
    return find("deviceId", Sort.ascending("label"), deviceId).list();
  }

  public Optional<DeviceAttributeEntity> findByDeviceIdAndName(String deviceId,
      String attributeName) {
    return find("deviceId = ?1 and attributeName = ?2", deviceId,
        attributeName).firstResultOptional();
  }

  public void deleteAttributesNotIn(String deviceId, List<String> attributeName) {
    delete("{'deviceId': ?1, 'attributeName':{'$nin':[?2]}}", deviceId, attributeName);
  }

  public void deleteByDeviceIdAndName(String deviceId, String attributeName) {
    delete("deviceId = ?1 and attributeName = ?2", deviceId, attributeName);
  }

}
