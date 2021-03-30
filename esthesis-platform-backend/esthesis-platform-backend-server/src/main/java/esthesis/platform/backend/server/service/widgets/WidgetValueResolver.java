package esthesis.platform.backend.server.service.widgets;

import esthesis.platform.backend.server.model.DashboardWidget;

public interface  WidgetValueResolver {
  Object getValue(DashboardWidget dashboardWidget);
}
