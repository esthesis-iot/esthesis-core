export class NifiProducerFactoryDto {
  id?: number;
  createdOn?: Date;
  factoryClass?: string;
  friendlyName?: string;
  version?: string;
  supportsTelemetryProduce?: boolean;
  configurationTemplate?: string;
}
