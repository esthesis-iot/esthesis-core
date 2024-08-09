import {DeviceDataImportDto} from "./device-data-import-dto";

export interface DeviceTextDataImportDto extends DeviceDataImportDto{
  data: string | null;
}
