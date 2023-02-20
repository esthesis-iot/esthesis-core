import {GroupProgressDto} from "./group-progress-dto";

export interface CampaignStatsDto {
  groupMembers?: [number];
  groupMembersReplied?: [number];
  membersContacted?: number;
  membersContactedButNotReplied?: number;
  membersReplied?: number;
  successRate?: number;
  allMembers?: number;
  progress?: number;
  duration?: string;
  stateDescription: string;

  // This is a placeholder to be calculated by the frontend component.
  // It is not filled-in by the backend.
  groupProgress: GroupProgressDto[];
}
