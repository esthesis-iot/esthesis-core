package esthesis.platform.backend.server.service;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import esthesis.platform.backend.server.dto.DashboardWidgetDTO;
import esthesis.platform.backend.server.mapper.DashboardWidgetMapper;
import esthesis.platform.backend.server.model.DashboardWidget;
import esthesis.platform.backend.server.repository.DashboardWidgetRepository;
import esthesis.platform.backend.server.service.widgets.WidgetValueResolver;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
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
  private final DashboardService dashboardService;
  private final Map<String, WidgetValueResolver> widgetValueResolvers;

  /**
   * Finds the widgets of the default-view dashboard of the logged-in user.
   */
  public List<DashboardWidgetDTO> getWidgets(long dashboardId) {
    return dashboardWidgetMapper
      .map(dashboardWidgetRepository.findByDashboardId(dashboardId));
  }

  /**
   * Finds a specific widget.
   *
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
    // If no dashboard Id is provided, associate widget with the currently default user
    // dashboard.
    if (dashboardWidgetDTO.getId() == 0) {
      dashboardWidgetDTO.setDashboard(dashboardService.getDashboard().getId());
    }

    return save(dashboardWidgetDTO);
  }

  public void deleteWidget(long widgetId) {
    deleteById(widgetId);
  }

  public DashboardWidgetDTO updateWidgetCoordinates(long widgetId, int x, int y, int columns,
    int rows) {
    return dashboardWidgetMapper.map(
      findEntityById(widgetId)
        .setGridX(x)
        .setGridY(y)
        .setGridCols(columns)
        .setGridRows(rows));
  }

  /**
   * Returns the last value for the requested widget.
   *
   * @param widgetId The Id of the widget ot find its latest value.
   */
  public Object getWidgetValue(long widgetId) {
    // Find the widget and obtain its configuration.
    DashboardWidget widget = findEntityById(widgetId);

    // Get a resolver for this type of widget.
    String resolver = widget.getType() + "Resolver";
    if (!widgetValueResolvers.containsKey(resolver)) {
      throw new QDoesNotExistException(
        MessageFormat
          .format("Could not get value for widget Id ''{0}'', type ''{1}'', as a ''{2}'' resolver "
            + "could not be found.", widgetId, widget.getType(), resolver));
    } else {
      return widgetValueResolvers.get(resolver).getValue(widget);
    }
  }
}

