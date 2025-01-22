import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateDevicesStatusDto extends DashboardUpdateDto {
  // Total devices.
  total: number;
  // Total devices with status disabled.
  disabled: number;
  // Total devices with status preregistered.
  preregistered: number;
  // Total devices with status registered.
  registered: number;
}
