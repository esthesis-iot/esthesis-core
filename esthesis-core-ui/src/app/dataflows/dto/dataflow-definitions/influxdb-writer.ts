import {
  DATAFLOW_TEMPLATE_IMAGE_REGISTRY,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS,
  DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
  DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES
} from "./templates";

export const DATAFLOW_DEFINITION_INFLUXDB_WRITER = {
  type: "influxdb-writer",
  title: "InfluxDB Writer",
  category: "Data writer",
  description: "A component handling telemetry and metadata messages from devices, updating an InfluxDB database.",
  icon: "assets/img/dataflows/influxdb.png",
  fields: [
    ...DATAFLOW_TEMPLATE_STATUS,
    ...DATAFLOW_TEMPLATE_MAIN,
    ...DATAFLOW_TEMPLATE_IMAGE_REGISTRY,
    {
      key: "config", wrappers: ["section"],
      props: {label: "InfluxDB database"},
      fieldGroup: [
        {
          key: "influx-url", type: "input", defaultValue: "http://influxdb:8086",
          props: {label: "URL"}
        }, {
          key: "influx-token", type: "input",
          props: {label: "Access token"}
        }, {
          key: "influx-org", type: "input", defaultValue: "esthesis",
          props: {label: "Organisation name"}
        }, {
          key: "influx-bucket", type: "input", defaultValue: "esthesis",
          props: {label: "Storage bucket name"},
        },
      ]
    },
    {
      key: "config.kafka", wrappers: ["section"],
      props: {label: "Kafka Broker"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        {
          key: "consumer-group", type: "input", defaultValue: "dfl-influxdb-writer",
          props: {label: "Kafka consumer group"}
        },
        {
          key: "telemetry-topic", type: "input", defaultValue: "esthesis-telemetry",
          props: {label: "Kafka topic to read telemetry messages from"}
        },
        {
          key: "metadata-topic", type: "input", defaultValue: "esthesis-metadata",
          props: {label: "Kafka topic to read metadata messages from"}
        },
      ]
    },
    ...DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES,
    ...DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
    ...DATAFLOW_TEMPLATE_LOGGING
  ]
};
