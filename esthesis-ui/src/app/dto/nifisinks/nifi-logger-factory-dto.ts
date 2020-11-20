import {BaseDto} from '../base-dto';

export class NiFiLoggerFactoryDto extends BaseDto {
  factoryClass: string;
  friendlyName: string;
  version: string;
}
