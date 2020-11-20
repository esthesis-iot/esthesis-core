import {BaseDto} from '../base-dto';

export class NifiReaderFactoryDto extends BaseDto {
  factoryClass: string;
  friendlyName: string;
  version: string;
  supportsPingRead: boolean;
  supportsMetadataRead: boolean;
  supportsTelemetryRead: boolean;
  configurationTemplate: string;
}
