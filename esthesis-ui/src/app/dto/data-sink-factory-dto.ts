export class DataSinkFactoryDto {
  factoryClass: string;
  friendlyName: string;
  supportsMetadataRead: boolean;
  supportsTelemetryRead: boolean;
  supportsMetadataWrite: boolean;
  supportsTelemetryWrite: boolean;
  version: string;
  configurationTemplate: string;
}
