import {BaseDto} from '../base-dto';

export interface NiFiSinkDto extends BaseDto {
  name: string;
  factoryClass: string;
  handler: number;
  state: boolean;
  configuration: string;
  type?: string;
  validationErrors?: string;
}
