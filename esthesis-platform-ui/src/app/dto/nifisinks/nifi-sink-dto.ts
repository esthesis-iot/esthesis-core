import {BaseDto} from '../base-dto';

export class NifiSinkDto extends BaseDto {
  name: string;
  factoryClass: string;
  handler: number;
  state: boolean;
  configuration: string;
  type: string;
  processorId: string;
  validationErrors: string;
}
