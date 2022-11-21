import {BaseDto} from "./base-dto";

export interface CommandReplyDto extends BaseDto {
  correlationId: string;
  hardwareId: string;
  success: boolean;
  output: string;
}
