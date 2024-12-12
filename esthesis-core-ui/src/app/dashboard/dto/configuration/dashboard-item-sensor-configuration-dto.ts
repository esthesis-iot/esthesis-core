export interface DashboardItemSensorConfigurationDto {
  hardwareId: string;
  measurement: string;
  sparkline: boolean;
  unit: string;
  icon: string;
  precision: number;
  sparklinePoints: number;
}
