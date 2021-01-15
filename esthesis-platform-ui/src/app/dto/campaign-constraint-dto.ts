import {BaseDto} from './base-dto';

export class CampaignConstraintDto extends BaseDto {
  constructor(type: number) {
    super();
    this.type = type;
  }
  name!: string;
  type!: number;  // See AppConstants.CAMPAIGN
  target!: number;
  stage!: number;
}
