package esthesis.services.device.impl.repository;

import esthesis.service.device.dto.DeviceProfileNote;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeviceProfileFieldRepository implements
    PanacheMongoRepository<DeviceProfileNote> {

  public List<DeviceProfileNote> findByDeviceId(String deviceId) {
    return find("deviceId", Sort.ascending("label"), deviceId).list();
  }

  public Optional<DeviceProfileNote> findByDeviceIdAndName(String deviceId,
      String fieldName) {
    return find("deviceId = ?1 and fieldName = ?2",
        deviceId, fieldName).firstResultOptional();
  }

  public void deleteFieldsNotIn(String deviceId, List<String> fieldNames) {
    delete("{'deviceId': ?1, 'fieldName':{'$nin':[?2]}}", deviceId, fieldNames);
  }

  public void deleteByDeviceIdAndName(String deviceId, String fieldName) {
    delete("deviceId = ?1 and fieldName = ?2", deviceId, fieldName);
  }

}
