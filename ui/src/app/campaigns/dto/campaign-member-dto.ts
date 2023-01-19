import {AppConstants} from "../../app.constants";

export class CampaignMemberDto {
  id?: string;

  // The type of this member, either a device or a tag.
  // See AppConstants.CAMPAIGN.MEMBER_TYPE
  type: string;

  // The identifier for this member, either a device hardware id or a tag name.
  identifier: string;

  // The group in which this member belongs to.
  group: number;

  // @ts-ignore
  constructor(id?: string, type: AppConstants.CAMPAIGN.MEMBER_TYPE, identifier: string,
    groupOrder: number) {
    this.id = id;
    this.type = type;
    this.identifier = identifier;
    this.group = groupOrder;
  }
}
