export interface DataflowMqttClientConfDto {
  tags: string[];
  topicPing: string;
  topicTelemetry: string;
  topicMetadata: string;
  topicControlRequest: string;
  topicControlReply: string;
}
