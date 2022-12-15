package esthesis.dataflows.influxdbwriter.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "esthesis.dfl")
public interface AppConfig {

  // The URL of the InfluxDB database to connect to.
  String influxUrl();

  // The access token to use.
  String influxToken();

  // The name of the organization to use.
  String influxOrg();

  // The name of the bucket to use.
  String influxBucket();

  // The Kafka consumer group id.
  Optional<String> kafkaConsumerGroup();

  // The Kafka topic to consume telemetry messages from.
  Optional<String> kafkaTelemetryTopic();

  // The Kafka topic to consume metadata messages from.
  Optional<String> kafkaMetadataTopic();

  // The URL of the Kafka cluster to connect to.
  String kafkaClusterUrl();

  // The number of messages that can be queued for processing.
  @WithDefault("1000")
  int queueSize();

  // How often the queue is polled for new messages (in milliseconds).
  @WithDefault("500")
  int pollTimeout();

  // The maximum number of concurrent consumers.
  @WithDefault("10")
  int consumers();
}
