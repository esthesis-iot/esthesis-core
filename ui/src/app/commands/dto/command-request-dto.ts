import {BaseDto} from "../../dto/base-dto";

export interface CommandRequestDto extends BaseDto {
  command: string;
  commandType: string;
  arguments: string;
  dispatchedOn: Date;
  executionType: string;
}
