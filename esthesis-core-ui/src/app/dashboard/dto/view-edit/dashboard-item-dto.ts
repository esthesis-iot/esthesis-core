import {BaseDto} from "../../../dto/base-dto";

export interface DashboardItemDto extends BaseDto {
  type: string;
  title: string;
  subtitle?: string;
  columns: number;
  index: number;
  configuration?: string;
}
