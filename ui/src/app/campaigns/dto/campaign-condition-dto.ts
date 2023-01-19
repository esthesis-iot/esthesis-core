export class CampaignConditionDto {
  constructor(type: string) {
    this.type = type;
  }

  id?: string;

  // The type of this condition, see AppConstants.CAMPAIGN.CONDITION.TYPE
  type: string;

  // The group in which this condition belongs to. Group '0' indicates the global group.
  group!: number;

  // The group stage in which this condition belongs to. See AppConstants.CAMPAIGN.CONDITION.STAGE.
  stage?: number;

  // Condition-type specific data.
  scheduleDate?: Date;
  scheduleHour?: number;
  scheduleMinute?: number;
  operation?: number;
  value?: string;
  propertyName?: string;
  propertyIgnorable?: boolean;
}
