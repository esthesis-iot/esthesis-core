import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateDiffDto extends DashboardUpdateDto {
  hardwareId: string;
  measurement: string;
  value: string;
}
