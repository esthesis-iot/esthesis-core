import {BaseDto} from "../../dto/base-dto";

export interface DashboardWidgetDto extends BaseDto {
  type: String;
  gridCols: number;
  gridRows: number;
  gridX: number;
  gridY: number;
  configuration: any;
  dashboard: number;
  updateEvery: number;
}
