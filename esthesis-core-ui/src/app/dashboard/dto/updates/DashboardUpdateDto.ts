import {AppConstants} from "../../../app.constants";

export interface DashboardUpdateDto {
  id: string;
  type: typeof AppConstants.DASHBOARD.ITEM.TYPE
}
