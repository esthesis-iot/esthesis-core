package esthesis.platform.backend.server.service;

import esthesis.platform.backend.server.dto.DashboardWidgetDTO;
import esthesis.platform.backend.server.mapper.DashboardWidgetMapper;
import esthesis.platform.backend.server.model.DashboardWidget;
import esthesis.platform.backend.server.repository.DashboardWidgetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class DashboardWidgetService extends BaseService<DashboardWidgetDTO, DashboardWidget> {

  private final DashboardWidgetRepository dashboardWidgetRepository;
  private final DashboardWidgetMapper dashboardWidgetMapper;

  /**
   * Finds the widgets of the default-view dashboard of the logged-in user.
   */
  public List<DashboardWidgetDTO> getWidgets(long dashbaordId) {
    return dashboardWidgetMapper
      .map(dashboardWidgetRepository.findByDashboardId(dashbaordId));
  }

  /**
   * Finds a specific widget.
   * @param widgetId The id of the widget to find.
   */
  public DashboardWidgetDTO getWidget(long widgetId) {
    return findById(widgetId);
  }

  /**
   * Saves a widget into a dashboard.
   *
   * @param dashboardWidgetDTO The widget configuration to persist.
   */
  public DashboardWidgetDTO saveWidget(DashboardWidgetDTO dashboardWidgetDTO) {
    System.out.println(dashboardWidgetDTO);
    // Associate new dashboard items with user's default dashboard.
//    if (dashboardWidgetDTO.getId() == 0) {
//      dashboardWidgetDTO.setDashboard(dashboard.getId());
//    }
//
//    return dashboardWidgetMapper.map(dashboardWidgetRepository
//      .save(dashboardWidgetMapper.map(dashboardWidgetDTO)));
    return save(dashboardWidgetDTO);
  }

  public void deleteWidget(long widgetId) {
    deleteById(widgetId);
  }

}
