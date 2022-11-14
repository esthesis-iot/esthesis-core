export interface CommandExecuteRequestDto {
  hardwareIds: string;
  tags: string;
  commandType: string;
  executionType: string;
  command: string;
  arguments: string;
  description: string;
}
