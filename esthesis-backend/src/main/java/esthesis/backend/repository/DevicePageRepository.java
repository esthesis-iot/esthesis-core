package esthesis.backend.repository;

import esthesis.backend.model.DevicePage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevicePageRepository extends BaseRepository<DevicePage> {
  List<DevicePage> findAllByShownIsTrue();
}
