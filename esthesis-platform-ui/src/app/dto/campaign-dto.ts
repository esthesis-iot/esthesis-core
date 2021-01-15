import {Validators} from '@angular/forms';
import {CampaignConstraintDto} from './campaign-constraint-dto';

export interface CampaignDto {
  name:string;
  description: string;
  scheduleDate: string;
  scheduleHour: number;
  scheduleMinute: number;
  type: number;
  commandName: string;
  commandArguments: string;
  provisioningPackageId: string;
  constraints: CampaignConstraintDto[];
  devicesAndTags: String[][];
}
