import {BaseDto} from "./base-dto";

export interface DeviceProfileNoteDto extends BaseDto {
  deviceId: string;
  fieldName: string;
  fieldValue?: string;
  label: string;
}
