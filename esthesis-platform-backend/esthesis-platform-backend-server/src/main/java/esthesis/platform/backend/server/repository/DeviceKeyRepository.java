package esthesis.platform.backend.server.repository;

import com.querydsl.jpa.impl.JPAQuery;
import esthesis.platform.backend.server.model.Device;
import esthesis.platform.backend.server.model.DeviceKey;
import esthesis.platform.backend.server.model.QDeviceKey;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceKeyRepository extends BaseRepository<DeviceKey>,
  QuerydslPredicateExecutor<DeviceKey>, DeviceKeyRepositoryExt {

}

interface DeviceKeyRepositoryExt {

  DeviceKey findLatestAccepted(long deviceAccepted);
}

class DeviceKeyRepositoryImpl implements DeviceKeyRepositoryExt {

  @PersistenceContext
  private EntityManager em;
  private static final QDeviceKey deviceKey = QDeviceKey.deviceKey;

  @Override
  public DeviceKey findLatestAccepted(long deviceId) {
    return new JPAQuery<DeviceKey>(em)
      .from(deviceKey)
      .where(deviceKey.rolledAccepted.eq(Boolean.TRUE)
        .and(deviceKey.device.eq(em.find(Device.class, deviceId))))
      .orderBy(deviceKey.rolledOn.desc())
      .fetchFirst();
  }
}
