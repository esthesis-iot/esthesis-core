import {BaseDto} from "./base-dto";

export interface ApplicationDto extends BaseDto {
  name: string;
  token: string;
  state: number;
}
