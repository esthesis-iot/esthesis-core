import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateCampaignDto extends DashboardUpdateDto {
  running: number;
  paused: number;
  terminated: number;
}
