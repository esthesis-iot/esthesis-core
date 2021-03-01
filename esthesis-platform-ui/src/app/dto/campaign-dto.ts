import {CampaignConditionDto} from './campaign-condition-dto';
import {CampaignMemberDto} from './campaign-member-dto';

export interface CampaignDto {
  id: number;
  name: string;
  description: string;
  scheduleDate: string;
  scheduleHour: number;
  scheduleMinute: number;
  type: number;
  commandName: string;
  commandArguments: string;
  provisioningPackageId: string;
  conditions: CampaignConditionDto[];
  members: CampaignMemberDto[];
  startedOn: Date;
  terminatedOn: Date;
}
