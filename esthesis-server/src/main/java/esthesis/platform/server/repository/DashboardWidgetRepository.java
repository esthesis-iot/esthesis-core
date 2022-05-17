package esthesis.platform.server.repository;

import esthesis.platform.server.model.DashboardWidget;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardWidgetRepository extends BaseRepository<DashboardWidget> {
  List<DashboardWidget> findByDashboardId(long dashboard);
}
