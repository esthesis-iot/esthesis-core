import {BaseDto} from './base-dto';

export interface CommandRequestDto extends BaseDto {
  operation: string;
  description: string;
  deviceHardwareId: string;
  id: number;
}
