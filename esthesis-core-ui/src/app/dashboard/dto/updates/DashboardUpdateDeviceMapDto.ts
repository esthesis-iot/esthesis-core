import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateDeviceMapDto extends DashboardUpdateDto {
  coordinates: string[];
}
