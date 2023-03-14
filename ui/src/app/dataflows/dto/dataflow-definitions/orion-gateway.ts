import {
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS,
  DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
  DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES
} from "./templates";

export const DATAFLOW_DEFINITION_FIWARE_ORION = {
  type: "orion-gateway",
  title: "FIWARE Orion gateway",
  category: "Gateway",
  description: "A gateway exposing esthesis devices to FIWARE Orion Context Broker.",
  icon: "assets/img/dataflows/fiware.png",
  fields: [
    ...DATAFLOW_TEMPLATE_STATUS,
    ...DATAFLOW_TEMPLATE_MAIN,
    {
      key: "config", wrappers: ["section"],
      props: {label: "Gateway configuration"},
      fieldGroup: [
        {
          key: "orion-url", type: "input",
          props: {label: "The URL of the Orion Context Broker, accessible from Kubernetes", required: true}
        },
        {
          key: "orion-default-type", type: "input", defaultValue: "Device",
          props: {label: "The Device Type to use when registering devices in Orion", required: true}
        },
        {
          key: "orion-type-attribute", type: "input",
          props: {label: "The name of a device attribute indicating the Device Type to use when registering devices in Orion"}
        },
        {
          key: "orion-id-attribute", type: "input",
          props: {label: "The name of a device attribute indicating the ID to use when registering devices in Orion"}
        },
        {
          key: "orion-id-prefix", type: "input",
          props: {label: "A prefix to add to the device ID when registering devices in Orion, when not using an attribute-defined ID"}
        },
        {
          key: "orion-registration-enabled-attribute", type: "input",
          props: {
            label: "A true/false device attribute indicating whether the device should be registered in Orion or not"
          }
        },
        {
          key: "orion-create-device", type: "select", defaultValue: "true",
          props: {
            label: "Create esthesis devices in Orion", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        {
          key: "orion-delete-device", type: "select", defaultValue: "false",
          props: {
            label: "Delete Orion device when deleted in esthesis", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        {
          key: "orion-update-data", type: "select", defaultValue: "true",
          props: {
            label: "Push device metrics and attributes to Orion", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        {
          key: "orion-retro-create-devices-on-schedule", type: "select", defaultValue: "false",
          props: {
            label: "Create esthesis devices in Orion based on a schedule", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        {
          key: "orion-retro-create-devices-schedule", type: "input", defaultValue: "0 0 0 * * ?",
          props: {label: "A cron expression for the schedule to create devices in Orion"}
        },
        {
          key: "orion-retro-create-devices-on-boot", type: "select", defaultValue: "true",
          props: {
            label: "Create esthesis devices in Orion when the gateway boots", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        {
          key: "orion-update-data-attribute", type: "input",
          props: {label: "A true/false device attribute indicating whether data for this device should be updated in Orion or not"}
        },

        {
          key: "attribute-esthesis-id", type: "input", defaultValue: "esthesisId",
          props: {label: "The name of the Orion attribute to hold the esthesis ID for this device", required: true}
        },
        {
          key: "attribute-esthesis-hardware-id", type: "input", defaultValue: "esthesisHardwareId",
          props: {label: "The name of the Orion attribute to hold the esthesis hardware ID for this device", required: true}
        },
        {
          key: "esthesis-orion-metadata-name", type: "input", required: true, defaultValue: "maintainedBy",
          props: {label: "The name of the metadata attribute to indicate an attribute is maintained by esthesis."}
        },
        {
          key: "esthesis-attribute-source-metadata-name", type: "input", required: true, defaultValue: "attributeSource",
          props: {label: "The name of the metadata attribute to indicate what was the source of an esthesis attribute."}
        },
        {
          key: "esthesis-orion-metadata-value", type: "input", required: true, defaultValue: "esthesis",
          props: {label: "The value of the metadata attribute to indicate an attribute is maintained by esthesis."}
        },
      ]
    },
    {
      key: "config.kafka", wrappers: ["section"],
      props: {label: "Kafka"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        {
          key: "consumer-group", type: "input", defaultValue: "dfl-orion-gateway",
          props: {label: "Kafka consumer group"}
        },
        {
          key: "telemetry-topic", type: "input",
          defaultValue: "esthesis-telemetry",
          props: {
            label: "The topic to read telemetry data from devices",
          },
        },
        {
          key: "metadata-topic", type: "input",
          defaultValue: "esthesis-metadata",
          props: {
            label: "The topic to read metadata data from devices",
          },
        },
        {
          key: "application-topic", type: "input",
          defaultValue: "esthesis-app",
          props: {
            label: "The topic to read platform notification data",
          },
        },
      ]
    },
    ...DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES,
    ...DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY,
    ...DATAFLOW_TEMPLATE_LOGGING
  ]
};
