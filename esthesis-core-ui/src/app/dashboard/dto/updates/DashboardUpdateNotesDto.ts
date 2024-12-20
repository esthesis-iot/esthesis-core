import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateNotesDto extends DashboardUpdateDto {
  gitVersion: string;
  gitCommitId: string;
  gitBuildTime: Date;
}
