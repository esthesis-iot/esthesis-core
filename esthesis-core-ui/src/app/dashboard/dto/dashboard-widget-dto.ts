import {BaseDto} from "../../dto/base-dto";

export interface DashboardWidgetDto extends BaseDto {
  type: string;
  subtitle?: string;
  columns: number;
  index: number;
  title: string;
  unit?: string;
  icon?: string;
  precision?: number;
}
