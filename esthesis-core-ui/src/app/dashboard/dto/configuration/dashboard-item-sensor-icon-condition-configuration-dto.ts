export class DashboardItemSensorIconConditionConfigurationDto {
  condition: string;
  icon: string;

  constructor(condition: string, icon: string) {
    this.condition = condition;
    this.icon = icon;
  }
}
