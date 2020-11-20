import {BaseDto} from './base-dto';

export class CommandRequestDto extends BaseDto {
  operation: string;
  description: string;
  deviceHardwareId: string;
  id: number;
}
