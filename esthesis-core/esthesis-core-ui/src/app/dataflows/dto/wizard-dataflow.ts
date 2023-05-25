export interface WizardDataflowDto {
  type: string;
  name: string;
  description?: string;
  status: boolean;
  config: {
    "mqtt-broker"?: {
      "cluster-url": string;
    }
    redis?: {
      url: string;
      "max-size": string;
      "ttl": string;
    }
    "influx-url"?: string;
    "influx-org"?: string;
    "influx-token"?: string;
    "influx-bucket"?: string;
    "mqtt-topic"?: {
      ping: string;
      telemetry: string;
      metadata: string;
      "command-request": string;
      "command-reply": string;
    }
    "kafka-topic"?: {
      ping: string;
      telemetry: string;
      metadata: string;
      "command-request": string;
      "command-reply": string;
    }
    kafka: {
      "cluster-url": string;
      "consumer-group"?: string;
      "ping-topic"?: string;
      "command-reply-topic"?: string;
      "telemetry-topic"?: string;
      "metadata-topic"?: string;
    }
    "esthesis-db-url"?: string;
    "esthesis-db-name"?: string;
    "esthesis-db-username"?: string;
    "esthesis-db-password"?: string;
    "queue-size"?: string;
    "poll-timeout"?: string;
    consumers?: string;
    logging: {
      common: string;
      esthesis: string;
    }
  };
  kubernetes: {
    namespace: string;
    "cpu-request": string;
    "cpu-limit": string;
    docker: string;
    "pods-min": string;
    "pods-max": string;
  };
}
