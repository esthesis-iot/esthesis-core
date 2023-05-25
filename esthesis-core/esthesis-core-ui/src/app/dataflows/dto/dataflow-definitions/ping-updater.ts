import {
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS,
  DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
  DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES
} from "./templates";

export const DATAFLOW_DEFINITION_PING_UPDATER = {
  type: "ping-updater",
  title: "Ping updater",
  category: "Data update",
  description: "A component handling ping messages from devices, updating their 'last seen' entry in  esthesis database.",
  icon: "assets/img/dataflows/radar.png",
  fields: [
    ...DATAFLOW_TEMPLATE_STATUS,
    ...DATAFLOW_TEMPLATE_MAIN,
    {
      key: "config.kafka", wrappers: ["section"],
      props: {label: "Kafka"},
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
    {
      key: "config", wrappers: ["section"],
      props: {label: "Esthesis database"},
      fieldGroup: [
        {
          key: "esthesis-db-url", type: "input", defaultValue: "mongodb://mongodb:27017",
          props: {label: "URL"}
        }, {
          key: "esthesis-db-name", type: "input", defaultValue: "esthesiscore",
          props: {label: "Database name"}
        }, {
          key: "esthesis-db-username", type: "input", defaultValue: "esthesis-system",
          props: {label: "Username"}
        }, {
          key: "esthesis-db-password", type: "input", defaultValue: "esthesis-system",
          props: {label: "Password", type: "password"},
        },
      ]
    },
    ...DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES,
    ...DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
    ...DATAFLOW_TEMPLATE_LOGGING
  ]
};
