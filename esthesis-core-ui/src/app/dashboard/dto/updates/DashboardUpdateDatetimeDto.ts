import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateDatetimeDto extends DashboardUpdateDto {
  serverDate: number;
}
