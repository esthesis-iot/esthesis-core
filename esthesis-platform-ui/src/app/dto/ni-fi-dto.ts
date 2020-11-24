import {BaseDto} from './base-dto';

export class NiFiDto extends BaseDto {

  name: string;
  description: string;
  url: string;
  wfVersion: string;
  synced: boolean;
  lastChecked: Date;
}
