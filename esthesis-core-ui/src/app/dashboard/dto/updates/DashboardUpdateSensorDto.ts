import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateSensorDto extends DashboardUpdateDto {
  hardwareId: string;
  measurement: string;
  value: string;
}
