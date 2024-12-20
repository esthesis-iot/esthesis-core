import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateSensorIconDto extends DashboardUpdateDto {
  hardwareId: string;
  measurement: string;
  value: string;
}
