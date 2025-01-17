import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateChartDto extends DashboardUpdateDto {
  data: {left: string, middle: string, right: string}[];
}
