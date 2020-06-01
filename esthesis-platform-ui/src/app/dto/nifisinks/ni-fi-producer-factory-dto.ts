import {BaseDto} from '../base-dto';

export class NiFiProducerFactoryDto extends BaseDto {
  factoryClass: string;
  friendlyName: string;
  version: string;
  supportsMetadataProduce: boolean;
  supportsTelemetryProduce: boolean;
  configurationTemplate: string;
}
