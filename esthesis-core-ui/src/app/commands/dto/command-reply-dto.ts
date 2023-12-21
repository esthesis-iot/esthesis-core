import {BaseDto} from "../../dto/base-dto";

export interface CommandReplyDto extends BaseDto {
  correlationId: string;
  hardwareId: string;
  success: boolean;
  output: string;
}
