import {GridsterItem} from "angular-gridster2";

/**
 * A wrapper for a DashboardWidgetDto to be included in the grid. The wrapper translates
 * DashboardWidgetDto to the properties Gridster expect as well as injecting additional
 * grid-required properties.
 */
export interface DashboardWidgetForGridDto {
  id: number;
  type: string; // See AppConstants.DASHBOARD.WIDGETS
  grid: GridsterItem;
  dashboardId: number;
}
