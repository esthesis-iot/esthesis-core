import {BaseDto} from './base-dto';

export interface NiFiDto extends BaseDto {
  name: string;
  description: string;
  url: string;
  dtUrl: string;
  wfVersion: string;
  synced: boolean;
  lastChecked: Date;
}
