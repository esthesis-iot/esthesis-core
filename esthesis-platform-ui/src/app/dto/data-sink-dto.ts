export class DataSinkDto {
  id: number;
  factoryClass: string;
  name: string;
  metadata: boolean;
  telemetry: boolean;
  state: boolean;
  configuration: string;
}
