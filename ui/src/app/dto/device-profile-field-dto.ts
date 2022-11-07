import {BaseDto} from "./base-dto";

export interface DeviceProfileFieldDto extends BaseDto {
  deviceId: string;
  fieldName: string;
  fieldValue?: string;
  label: string;
}
