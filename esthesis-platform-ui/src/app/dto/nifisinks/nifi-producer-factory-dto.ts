import {BaseDto} from '../base-dto';

export class NifiProducerFactoryDto extends BaseDto {
  factoryClass: string;
  friendlyName: string;
  version: string;
  supportsTelemetryProduce: boolean;
  configurationTemplate: string;
}
