export class MqttServerDto {
  id: string;
  name: string;
  ipAddress: string;
  state: boolean;
  tags: number[];
  topicTelemetry: string;
  topicControl: string;
  topicMetadata: string;
}
