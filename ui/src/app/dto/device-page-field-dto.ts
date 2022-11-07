import {BaseDto} from "./base-dto";

export interface DevicePageFieldDto extends BaseDto {
  measurement: string;
  shown: boolean;
  label: string;
  formatter: string;
}
