import {BaseDto} from "../../dto/base-dto";

export interface DeviceAttributeDto extends BaseDto {
  deviceId: string;
  fieldName: string;
  fieldValue?: string;
  label: string;
}
