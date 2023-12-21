import {BaseDto} from "../../dto/base-dto";

export interface ApplicationDto extends BaseDto {
  name: string;
  token: string;
  state: number;
}
