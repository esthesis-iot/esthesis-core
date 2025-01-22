import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateAboutDto extends DashboardUpdateDto {
  gitVersion: string;
  gitCommitId: string;
  gitBuildTime: Date;
}
