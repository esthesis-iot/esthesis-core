import {BaseDto} from "../../../dto/base-dto";
import {DashboardItemDto} from "./dashboard-item-dto";

export interface DashboardDto extends BaseDto {
  id: string;
  name: string;
  description: string;
  shared: boolean;
  home: boolean;
  items: DashboardItemDto[];
}
