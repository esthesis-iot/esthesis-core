import {
  DATAFLOW_TEMPLATE_CONCURRENCY,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_KUBERNETES,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_MONGODB,
  DATAFLOW_TEMPLATE_STATUS
} from "./templates";

export const DATAFLOW_DEFINITION_PING_UPDATER = {
  type: "ping-updater",
  title: "Ping updater",
  category: "Data update",
  description: "A component handling ping messages from devices, updating their 'last seen' entry in  esthesis database.",
  icon: "assets/img/dataflows/radar.png",
  fields: [
    { fieldGroup: DATAFLOW_TEMPLATE_STATUS },
    { fieldGroup: DATAFLOW_TEMPLATE_MAIN },
    { key: "config.kafka", wrappers: ["section"], props: {label: "Kafka"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        {
          key: "consumer-group", type: "input", defaultValue: "dfl-ping-updater",
          props: {label: "Kafka consumer group"}
        },
        {
          key: "ping-topic", type: "input", defaultValue: "esthesis-ping",
          props: {label: "Kafka topic to read ping messages from", required: true}
        }
      ]
    },
    { key: "config.esthesis-db", wrappers: ["section"], props: {label: "esthesis CORE database"},
      fieldGroup: DATAFLOW_TEMPLATE_MONGODB },
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
