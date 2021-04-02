package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.DashboardWidget;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardWidgetRepository extends BaseRepository<DashboardWidget> {
  List<DashboardWidget> findByDashboardId(long dashboard);
}
