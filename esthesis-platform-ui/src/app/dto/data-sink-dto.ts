export class DataSinkDto {
  id: number;
  factoryClass: string;
  name: string;
  metadataRead: boolean;
  telemetryRead: boolean;
  metadataWrite: boolean;
  telemetryWrite: boolean;
  state: boolean;
  configuration: string;
}
