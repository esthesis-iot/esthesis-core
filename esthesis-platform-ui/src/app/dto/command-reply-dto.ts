import {BaseDto} from './base-dto';

export interface CommandReplyDto extends BaseDto {
  id: number;
  payload: string;
  payloadType: string;
  payloadEncoding: string;
}
