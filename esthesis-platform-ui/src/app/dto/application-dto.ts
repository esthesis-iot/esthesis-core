import {BaseDto} from './base-dto';

export class ApplicationDto extends BaseDto {
  name: string;
  token: string;
  state: number;
}
