import {
  DATAFLOW_TEMPLATE_CONCURRENCY,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_KUBERNETES,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_REDIS,
  DATAFLOW_TEMPLATE_STATUS
} from "./templates";

export const DATAFLOW_DEFINITION_REDIS_CACHE = {
  type: "redis-cache",
  title: "Redis cache",
  category: "Digital Twins",
  description: "A component caching values from devices to a Redis store to be served as part of the Digital Twins API.",
  icon: "assets/img/dataflows/redis.png",
  fields: [
    { fieldGroup: DATAFLOW_TEMPLATE_STATUS },
    { fieldGroup: DATAFLOW_TEMPLATE_MAIN },
    { key: "config.redis", wrappers: ["section"],
      props: { label: "Redis" },
      fieldGroup: DATAFLOW_TEMPLATE_REDIS },
    { key: "config.kafka", wrappers: ["section"],
      props: { label: "Kafka" },
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
    { key: "config.kubernetes", wrappers: ["section"], props: {label: "Kubernetes"},
      fieldGroup: DATAFLOW_TEMPLATE_KUBERNETES
    },
    { key: "config.concurrency", wrappers: ["section"], props: {label: "Concurrency"},
      fieldGroup: DATAFLOW_TEMPLATE_CONCURRENCY
    },
    { key: "config.logging", wrappers: ["section"], props: {label: "Logging"},
      fieldGroup: DATAFLOW_TEMPLATE_LOGGING
    }
  ]
};
