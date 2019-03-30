export class DeviceDto {
  id: string;
  device: string;
  lastSeen?: Date;
  owner?: string;
  status?: string;
  connection?: string;
  firmware?: string;
}
