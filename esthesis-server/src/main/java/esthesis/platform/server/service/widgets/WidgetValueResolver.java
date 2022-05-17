package esthesis.platform.server.service.widgets;

import esthesis.platform.server.model.DashboardWidget;

public interface  WidgetValueResolver {
  Object getValue(DashboardWidget dashboardWidget);
}
