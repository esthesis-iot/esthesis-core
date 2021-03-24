import {GridsterItem} from "angular-gridster2";

export interface DashboardWidgetForGridDto {
  id: number;
  type: string; // See AppConstants.DASHBOARD.WIDGETS
  grid: GridsterItem;
  dashboardId: number;
}
