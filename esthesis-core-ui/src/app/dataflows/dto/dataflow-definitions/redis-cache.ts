import {
  DATAFLOW_TEMPLATE_IMAGE_REGISTRY,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS,
  DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
  DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES
} from "./templates";

export const DATAFLOW_DEFINITION_REDIS_CACHE = {
  type: "redis-cache",
  title: "Redis cache",
  category: "Digital Twins",
  description: "A component caching values from devices to a Redis store to be served as part of the Digital Twins API.",
  icon: "assets/img/dataflows/redis.png",
  fields: [
    ...DATAFLOW_TEMPLATE_STATUS,
    ...DATAFLOW_TEMPLATE_MAIN,
    ...DATAFLOW_TEMPLATE_IMAGE_REGISTRY,
    {
      key: "config.redis", wrappers: ["section"],
      props: {label: "Redis"},
      fieldGroup: [
        {
          key: "url", type: "input", defaultValue: "redis://:esthesis-system@redis-headless:6379/0",
          props: {label: "The url of the Redis store", required: true, hintStart: "e.g. redis://username:password@server:port/database"}
        },
        {
          key: "max-size", type: "input", defaultValue: "1024",
          props: {
            label: "The maximum value size (in bytes) eligible for caching", required: true,
            hintStart: "This value pertains to device measurements caching, provisioning packages will be cached regardless of size."
          }
        },
        {
          key: "ttl", type: "input", defaultValue: "0",
          props: {
            label: "Time to live (in minutes), set to 0 to never automatically expire cached entries.",
            required: true, hintStart: "This value pertains to device measurements caching, provisioning packages will be cached forever."
          }
        }
      ]
    }, {
      key: "config.kafka", wrappers: ["section"],
      props: {label: "Kafka"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        {
          key: "consumer-group", type: "input", defaultValue: "dfl-redis-cache",
          props: {label: "Kafka consumer group"}
        },
        {
          key: "telemetry-topic", type: "input", defaultValue: "esthesis-telemetry",
          props: {label: "Kafka topic to read telemetry messages from", required: true}
        },
        {
          key: "metadata-topic", type: "input", defaultValue: "esthesis-metadata",
          props: {label: "Kafka topic to read metadata messages from", required: true}
        }
      ]
    },
    ...DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES,
    ...DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
    ...DATAFLOW_TEMPLATE_LOGGING
  ]
};
