import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateSecurityStatsDto extends DashboardUpdateDto {
  users: number;
  groups: number;
  roles: number;
  policies: number;
  audits: number;
}
