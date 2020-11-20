import {BaseDto} from '../base-dto';

export class NiFiWriterFactoryDto extends BaseDto {
  factoryClass: string;
  friendlyName: string;
  version: string;
  supportsPingWrite: boolean;
  supportsMetadataWrite: boolean;
  supportsTelemetryWrite: boolean;
  configurationTemplate: string;
}
