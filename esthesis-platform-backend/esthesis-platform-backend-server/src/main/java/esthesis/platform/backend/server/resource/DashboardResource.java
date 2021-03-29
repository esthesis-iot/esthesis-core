package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import esthesis.platform.backend.server.dto.DashboardWidgetDTO;
import esthesis.platform.backend.server.service.DashboardService;
import esthesis.platform.backend.server.service.DashboardWidgetService;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardResource {

  private final DashboardService dashboardService;
  private final DashboardWidgetService dashboardWiddgetService;

  @GetMapping(path = "widget", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get dashboard entries.")
  @ReplyFilter("-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  public List<DashboardWidgetDTO> getWidgets() {
    return dashboardWiddgetService.getWidgets(dashboardService.getDashboard().getId());
  }

  @DeleteMapping(path = "widget/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not delete widget entries.")
  public void deleteWidget(@PathVariable long id) {
    dashboardWiddgetService.deleteWidget(id);
  }

  @GetMapping(path = "widget/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get widget.")
  @ReplyFilter("-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  public DashboardWidgetDTO getWidget(@PathVariable long id) {
    return dashboardWiddgetService.getWidget(id);
  }

  @PostMapping(path = "widget", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not save widget.")
  @ReplyFilter("-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  public DashboardWidgetDTO saveWidget(@Valid @RequestBody DashboardWidgetDTO dashboardWidgetDTO) {
    return dashboardWiddgetService.saveWidget(dashboardWidgetDTO);
  }

  @PutMapping(path = "widget/{id}/{x},{y}/{columns},{rows}", produces =
    MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not update widget's coordinates.")
  public DashboardWidgetDTO updateWidgetCoordinates(@PathVariable long id, @PathVariable int x,
    @PathVariable int y, @PathVariable int columns, @PathVariable int rows) {
    return dashboardWiddgetService.updateWidgetCoordinates(id, x, y, columns, rows);
  }
}
