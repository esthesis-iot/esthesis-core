import {
  DATAFLOW_TEMPLATE_CONCURRENCY,
  DATAFLOW_TEMPLATE_INFLUXDB,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_KUBERNETES,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS
} from "./templates";

export const DATAFLOW_DEFINITION_INFLUXDB_WRITER = {
  type: "influxdb-writer",
  title: "InfluxDB Writer",
  category: "Data writer",
  description: "A component handling telemetry and metadata messages from devices, updating an InfluxDB database.",
  icon: "assets/img/dataflows/influxdb.png",
  fields: [
    { fieldGroup: DATAFLOW_TEMPLATE_STATUS },
    { fieldGroup: DATAFLOW_TEMPLATE_MAIN },
    { key: "config.influx", wrappers: ["section"], props: {label: "esthesis CORE database"},
      fieldGroup: DATAFLOW_TEMPLATE_INFLUXDB
    },
    { key: "config.kafka", wrappers: ["section"], props: {label: "Kafka Broker"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        { key: "consumer-group", type: "input", defaultValue: "dfl-influxdb-writer",
          props: {label: "Kafka consumer group"}
        },
        { key: "telemetry-topic", type: "input", defaultValue: "esthesis-telemetry",
          props: {label: "Kafka topic to read telemetry messages from"}
        },
        { key: "metadata-topic", type: "input", defaultValue: "esthesis-metadata",
          props: {label: "Kafka topic to read metadata messages from"}
        },
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
