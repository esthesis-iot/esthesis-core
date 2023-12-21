export class RuleDto {
  root!: string;
  system!: string;
  subsystem!: string;
  objectType!: string;
  objectId!: string;
  operation!: string;
  permission!: string;

  static deserialize(ernString: string): RuleDto {
    const ern = ernString.split(":");
    const ruleDto = new RuleDto();
    ruleDto.root = ern[0] ? ern[0].toLowerCase() : "";
    ruleDto.system = ern[1] ? ern[1].toLowerCase() : "";
    ruleDto.subsystem = ern[2] ? ern[2].toLowerCase() : "";
    ruleDto.objectType = ern[3] ? ern[3].toUpperCase() : "";
    ruleDto.objectId = ern[4] ? ern[4].toLowerCase() : "";
    ruleDto.operation = ern[5] ? ern[5].toUpperCase() : "";
    ruleDto.permission = ern[6] ? ern[6].toUpperCase() : "";
    return ruleDto;
  }

  static serialize(ruleDto: RuleDto): string {
    return ruleDto.root.toLowerCase() + ":"
    + ruleDto.system.toLowerCase() + ":"
    + ruleDto.subsystem.toLowerCase() + ":"
    + ruleDto.objectType.toLowerCase() + ":"
    + ruleDto.objectId.toLowerCase() + ":"
    + ruleDto.operation.toLowerCase() + ":"
    + ruleDto.permission.toLowerCase();
  }
}
