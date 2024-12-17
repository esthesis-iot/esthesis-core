export interface DashboardItemSensorConfigurationDto {
  hardwareId: string;
  measurement: string;
  sparkline: boolean;
  unit: string;
  icon: string;
  precision: number;
  sparklinePoints: number;

  threshold: boolean;
  thresholdLow: number;
  thresholdLowColor: string;
  thresholdMiddle: number;
  thresholdMiddleColor: string;
  thresholdHigh: number;
  thresholdHighColor: string;
}
