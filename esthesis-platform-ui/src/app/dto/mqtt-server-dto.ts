export class MqttServerDto {
  id: string;
  name: string;
  ipAddress: string;
  aIpAddress: string;
  state: boolean;
  tags: number[];
  topicTelemetry: string;
  topicControl: string;
  topicMetadata: string;
  caCert: string;
  clientCert: string;
  clientKey: string;
}
