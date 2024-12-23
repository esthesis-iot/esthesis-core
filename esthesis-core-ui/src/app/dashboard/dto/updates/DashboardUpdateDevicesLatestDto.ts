import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateDevicesLatestDto extends DashboardUpdateDto {
  devices: [{
    hardwareId: string;
    registeredOn: Date;
    type: string;
  }]
}
