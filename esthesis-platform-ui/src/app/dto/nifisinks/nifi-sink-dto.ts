import {BaseDto} from '../base-dto';

export class NiFiSinkDto extends BaseDto {
  name: string;
  factoryClass: string;
  handler: number;
  state: boolean;
  configuration: string;
  type: string;
  validationErrors: string;
}
