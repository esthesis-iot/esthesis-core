import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateDevicesLastSeenDto extends DashboardUpdateDto {
  lastMonth: string;
  lastWeek: string;
  lastDay: string;
  lastHour: string;
  lastMinute: string;
}
