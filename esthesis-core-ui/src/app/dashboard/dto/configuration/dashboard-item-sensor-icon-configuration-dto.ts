import {
  DashboardItemSensorIconConditionConfigurationDto
} from "./dashboard-item-sensor-icon-condition-configuration-dto";

export interface DashboardItemSensorIconConfigurationDto {
  hardwareId: string;
  measurement: string;
  unit: string;
  precision: number;
  conditions: DashboardItemSensorIconConditionConfigurationDto[];
}
