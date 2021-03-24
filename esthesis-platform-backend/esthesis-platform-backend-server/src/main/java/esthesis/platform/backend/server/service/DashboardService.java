package esthesis.platform.backend.server.service;

import esthesis.platform.backend.server.dto.DashboardDTO;
import esthesis.platform.backend.server.mapper.DashboardMapper;
import esthesis.platform.backend.server.model.Dashboard;
import esthesis.platform.backend.server.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class DashboardService extends BaseService<DashboardDTO, Dashboard> {

  private final DashboardRepository dashboardRepository;
  private final DashboardMapper dashboardMapper;

  /**
   * Finds the default dashboard for the currently logged in user. If such as dashboard does not
   * exist, it creates a new default dashboard.
   */
  public DashboardDTO getDashboard() {
    return dashboardMapper.map(
      dashboardRepository.findByUserIdAndDefaultViewIsTrue(getUserId()).orElseGet(()
        -> dashboardRepository.save(new Dashboard()
        .setDefaultView(true)
        .setShared(false)
        .setUserId(getUserId())))
    );
  }
}
