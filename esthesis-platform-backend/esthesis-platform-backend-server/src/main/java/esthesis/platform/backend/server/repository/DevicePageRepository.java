package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.DevicePage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevicePageRepository extends BaseRepository<DevicePage> {
  List<DevicePage> findAllByShownIsTrue();
}
