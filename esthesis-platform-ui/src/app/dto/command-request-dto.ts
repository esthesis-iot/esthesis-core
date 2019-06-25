import {BaseDto} from './base-dto';

export class CommandRequestDto extends BaseDto {
  command: string;
  description: string;
}
