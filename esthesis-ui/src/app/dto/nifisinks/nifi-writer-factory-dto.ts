export class NiFiWriterFactoryDto {
  id?: number;
  createdOn?: Date;
  factoryClass?: string;
  friendlyName?: string;
  version?: string;
  supportsPingWrite?: boolean;
  supportsMetadataWrite?: boolean;
  supportsTelemetryWrite?: boolean;
  configurationTemplate?: string;
}
