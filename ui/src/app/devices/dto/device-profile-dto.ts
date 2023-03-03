import {DeviceAttributeDto} from "./device-attribute-dto";
import {DevicePageFieldDataDto} from "./device-page-field-data-dto";

export interface DeviceProfileDto {
  attributes: DeviceAttributeDto[];
  fields: DevicePageFieldDataDto[];
}
