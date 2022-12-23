import {BaseDto} from "../../dto/base-dto";

export interface DevicePageFieldDto extends BaseDto {
  measurement: string;
  shown: boolean;
  label: string;
  formatter: string;
  icon: string;
}
