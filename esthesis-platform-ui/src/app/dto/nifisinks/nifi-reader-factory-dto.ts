export class NifiReaderFactoryDto {
  id?: number;
  createdOn?: Date;
  factoryClass?: string;
  friendlyName?: string;
  version?: string;
  supportsPingRead?: boolean;
  supportsMetadataRead?: boolean;
  supportsTelemetryRead?: boolean;
  configurationTemplate?: string;
}
