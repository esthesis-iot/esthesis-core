package esthesis.platform.server.repository;

import esthesis.platform.server.model.Dashboard;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends BaseRepository<Dashboard> {
  Optional<Dashboard> findByUserIdAndDefaultViewIsTrue(String userId);
}
