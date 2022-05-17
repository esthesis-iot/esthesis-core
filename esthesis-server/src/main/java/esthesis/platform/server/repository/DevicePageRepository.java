package esthesis.platform.server.repository;

import esthesis.platform.server.model.DevicePage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevicePageRepository extends BaseRepository<DevicePage> {
  List<DevicePage> findAllByShownIsTrue();
}
