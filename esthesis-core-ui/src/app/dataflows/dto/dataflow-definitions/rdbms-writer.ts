import {
  DATAFLOW_TEMPLATE_IMAGE_REGISTRY,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS,
  DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
  DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES
} from "./templates";

export const DATAFLOW_DEFINITION_RDBMS_WRITER = {
  type: "rdbms-writer",
  title: "RDBMS Writer",
  category: "Data writer",
  description: "A component handling telemetry and metadata messages from devices, updating a variety of relational databases.",
  icon: "assets/img/dataflows/rdbms.png",
  fields: [
    ...DATAFLOW_TEMPLATE_STATUS,
    ...DATAFLOW_TEMPLATE_MAIN,
    ...DATAFLOW_TEMPLATE_IMAGE_REGISTRY,
    {
      key: "config", wrappers: ["section"],
      props: {label: "Relational database"},
      fieldGroup: [
        {
          key: "db-kind", type: "select",
          props: {
            label: "Database vendor", required: true, options: [
              {value: "db2", label: "DB2"},
              {value: "derby", label: "Derby"},
              {value: "mariadb", label: "MariaDB"},
              {value: "mssql", label: "Microsoft SQL Server"},
              {value: "mysql", label: "MySQL"},
              {value: "oracle", label: "Oracle"},
              {value: "postgresql", label: "PostgreSQL"},
            ],
          }
        }, {
          key: "db-jdbc-url", type: "input", defaultValue: "jdbc:",
          props: {label: "URL", required: true}
        }, {
          key: "db-username", type: "input", defaultValue: "esthesis",
          props: {label: "Username", required: true}
        }, {
          key: "db-password", type: "input", defaultValue: "esthesis",
          props: {label: "Password", required: true}
        }, {
          key: "db-storage-strategy", type: "select",
          props: {
            label: "Data storage strategy", required: true, options: [
              {value: "SINGLE", label: "(SINGLE) Single table for all measurements"},
              {value: "MULTI", label: "(MULTI) Each measurement category on its own table"},
            ],
          }
        }, {
          key: "db-storage-strategy-single-table-name", type: "input", defaultValue: "measurements",
          props: {label: "Single strategy table name to hold the measurements", required: false}
        }, {
          key: "db-storage-strategy-single-key-name", type: "input", defaultValue: "esthesis_key",
          props: {label: "Single strategy column name to hold the name of a measurement", required: false}
        }, {
          key: "db-storage-strategy-single-value-name", type: "input", defaultValue: "esthesis_value",
          props: {label: "Single strategy column name to hold the value of a measurement", required: false}
        }, {
          key: "db-storage-strategy-single-timestamp-name", type: "input", defaultValue: "esthesis_timestamp",
          props: {label: "Single strategy column name to hold the value of the timestamp of a measurement", required: false}
        }, {
          key: "db-storage-strategy-single-hardware-id-name", type: "input", defaultValue: "esthesis_hardware_id",
          props: {label: "Single strategy column name to hold the value of the hardware ID of a measurement", required: false}
        }, {
          key: "db-storage-strategy-multi-timestamp-name", type: "input", defaultValue: "esthesis_timestamp",
          props: {label: "Multi strategy column name to hold the value of the timestamp of a measurement", required: false}
        }, {
          key: "db-storage-strategy-multi-hardware-id-name", type: "input", defaultValue: "esthesis_hardware_id",
          props: {label: "Multi strategy column name to hold the value of the hardware ID of a measurement", required: false}
        }
      ]
    },
    {
      key: "config.kafka", wrappers: ["section"],
      props: {label: "Kafka"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        {
          key: "consumer-group", type: "input", defaultValue: "dfl-rdbms-writer",
          props: {label: "Kafka consumer group"}
        },
        {
          key: "telemetry-topic", type: "input", defaultValue: "esthesis-telemetry",
          props: {label: "Kafka topic to read telemetry messages from"}
        },
        {
          key: "metadata-topic", type: "input", defaultValue: "esthesis-metadata",
          props: {label: "Kafka topic to read metadata messages from"}
        }
      ]
    },
    ...DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES,
    ...DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
    ...DATAFLOW_TEMPLATE_LOGGING
  ]
};
