export class CampaignConditionDto {
  constructor(type: number) {
    this.type = type;
  }

  // See AppConstants.CAMPAIGN
  id?: number;
  type: number;
  target!: number;
  stage?: number;
  scheduleDate?: Date;
  scheduleHour?: number;
  scheduleMinute?: number;
  operation?: number;
  value?: string;
  propertyName?: string;

}
