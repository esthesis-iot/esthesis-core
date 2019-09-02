import {BaseDto} from './base-dto';

export class CommandReplyDto extends BaseDto {
  id: number;
  payload: string;
}
