export interface DeviceDto {
  id: string;
  device: string;
  lastSeen?: Date;
  owner?: string;
  status?: string;
  connection?: string;
  firmware?: string;
  hardwareId: string;
  createdOn?: Date;
  registeredOn?: Date;
}
