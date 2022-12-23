import {BaseDto} from "../../dto/base-dto";

export interface DeviceProfileNoteDto extends BaseDto {
  deviceId: string;
  fieldName: string;
  fieldValue?: string;
  label: string;
}
