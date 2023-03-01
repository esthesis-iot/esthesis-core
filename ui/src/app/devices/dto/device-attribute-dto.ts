import {BaseDto} from "../../dto/base-dto";

export interface DeviceAttributeDto extends BaseDto {
  deviceId: string;
  attributeName: string;
  attributeValue: string;
}
