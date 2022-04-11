export class CampaignMemberDto {
  type: number;
  identifier: string;
  groupOrder: number;

  constructor(type: number, identifier: string, groupOrder: number) {
    this.type = type;
    this.identifier = identifier;
    this.groupOrder = groupOrder;
  }
}
