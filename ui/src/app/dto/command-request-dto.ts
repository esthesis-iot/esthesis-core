import {BaseDto} from "./base-dto";

export interface CommandRequestDto extends BaseDto {
  command: string;
  commandType: string;
  arguments: string;
  dispatchedOn: Date;
  executionType: string;
}
