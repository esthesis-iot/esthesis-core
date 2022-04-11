package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.Dashboard;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends BaseRepository<Dashboard> {
  Optional<Dashboard> findByUserIdAndDefaultViewIsTrue(String userId);
}
