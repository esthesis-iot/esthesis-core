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
  eta?: string;
}
