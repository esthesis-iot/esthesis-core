import {
  DATAFLOW_TEMPLATE_CONCURRENCY,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_KUBERNETES,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS
} from "./templates";

export const DATAFLOW_DEFINITION_FIWARE_ORION = {
  type: "orion-gateway",
  title: "FIWARE Orion gateway",
  category: "Gateway",
  description: "A gateway exposing esthesis devices to FIWARE Orion Context Broker.",
  icon: "assets/img/dataflows/fiware.png",
  fields: [
    { fieldGroup: DATAFLOW_TEMPLATE_STATUS },
    { fieldGroup: DATAFLOW_TEMPLATE_MAIN },
    { key: "config", wrappers: ["section"], props: {label: "Gateway configuration"},
      fieldGroup: [
        { key: "orion-url", type: "input",
          props: {label: "The URL of the Orion Context Broker, accessible from Kubernetes", required: true}
        },
        { key: "orion-default-type", type: "input", defaultValue: "Device",
          props: {label: "The Device Type to use when registering devices in Orion", required: true}
        },
        { key: "orion-type-attribute", type: "input",
          props: {label: "The name of a device attribute indicating the Device Type to use when registering devices in Orion"}
        },
        { key: "orion-id-attribute", type: "input",
          props: {label: "The name of a device attribute indicating the ID to use when registering devices in Orion"}
        },
        { key: "orion-id-prefix", type: "input",
          props: {label: "A prefix to add to the device ID when registering devices in Orion, when not using an attribute-defined ID"}
        },
        { key: "orion-registration-enabled-attribute", type: "input",
          props: {
            label: "A true/false device attribute indicating whether the device should be registered in Orion or not"
          }
        },
        { key: "orion-create-device", type: "select", defaultValue: "true",
          props: {
            label: "Create esthesis devices in Orion", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        { key: "orion-delete-device", type: "select", defaultValue: "false",
          props: {
            label: "Delete Orion device when deleted in esthesis", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        { key: "orion-update-data", type: "select", defaultValue: "true",
          props: {
            label: "Push device metrics and attributes to Orion", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        { key: "orion-retro-create-devices-on-schedule", type: "select", defaultValue: "false",
          props: {
            label: "Create esthesis devices in Orion based on a schedule", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        { key: "orion-retro-create-devices-schedule", type: "input", defaultValue: "0 0 0 * * ?",
          props: {label: "A cron expression for the schedule to create devices in Orion"}
        },
        { key: "orion-retro-create-devices-on-boot", type: "select", defaultValue: "true",
          props: {
            label: "Create esthesis devices in Orion when the gateway boots", options: [
              {value: "true", label: "Yes"},
              {value: "false", label: "No"}
            ],
          }
        },
        { key: "orion-update-data-attribute", type: "input",
          props: {label: "A true/false device attribute indicating whether data for this device should be updated in Orion or not"}
        },
        { key: "attribute-esthesis-id", type: "input", defaultValue: "esthesisId",
          props: {label: "The name of the Orion attribute to hold the esthesis ID for this device", required: true}
        },
        { key: "attribute-esthesis-hardware-id", type: "input", defaultValue: "esthesisHardwareId",
          props: {label: "The name of the Orion attribute to hold the esthesis hardware ID for this device", required: true}
        },
        { key: "esthesis-orion-metadata-name", type: "input", required: true, defaultValue: "maintainedBy",
          props: {label: "The name of the metadata attribute to indicate an attribute is maintained by esthesis."}
        },
        { key: "esthesis-attribute-source-metadata-name", type: "input", required: true, defaultValue: "attributeSource",
          props: {label: "The name of the metadata attribute to indicate what was the source of an esthesis attribute."}
        },
        { key: "esthesis-orion-metadata-value", type: "input", required: true, defaultValue: "esthesis",
          props: {label: "The value of the metadata attribute to indicate an attribute is maintained by esthesis."}
        },
        { key: "orion-ld-defined-contexts-url", type: "input", required: true,
          defaultValue: "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld",
          props: {label: "The URL of the Orion LD Contexts separated by comma", required: true}
        },
        { key: "orion-ld-defined-contexts-relationships", type: "input", required: true,
          defaultValue: "http://www.w3.org/ns/json-ld#context",
          props: {label: "The relationships of the Orion LD Contexts defined separated by comma", required: true}
        },
        { key: "orion-custom-measurement-json-format-attribute-name", type: "input",
          props: {label: "A custom measurement JSON structure formatted using the Qute engine template"}
        },
        { key: "orion-attributes-to-sync", type: "input",
          props: {label: "A list of attribute/measurement names, separated by commas, to be the only ones synchronized and sent to Orion"}
        },
        {
          key: "orion-authentication-type", type: "select", defaultValue: "NONE",
          props: {
            label: "Type of authentication", required: true, options: [
              {value: "NONE", label: "No authentication"},
              {value: "KEYROCK", label: "Keyrock oauth2"},
            ],
          }
        },
        { key: "orion-authentication-url", type: "input",
          props: {label: "The URL to be used for authentication"}
        },
        { key: "orion-authentication-credential-token", type: "input",
          props: {label: "The credential token to be used for authentication "}
        },
        { key: "orion-authentication-username", type: "input",
          props: {label: "The username to be used for authentication"}
        },
        { key: "orion-authentication-password", type: "input",
          props: {label: "The password to be used for authentication", type: "password"}
        },
        { key: "orion-authentication-grant-type", type: "input",
          props: {label: "The grant type to be used for authentication"}
        },
      ]
    },
    { key: "config.kafka", wrappers: ["section"], props: {label: "Kafka"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        { key: "consumer-group", type: "input", defaultValue: "dfl-orion-gateway",
          props: {label: "Kafka consumer group"}
        },
        { key: "telemetry-topic", type: "input", defaultValue: "esthesis-telemetry",
          props: {
            label: "The topic to read telemetry data from devices",
          },
        },
        { key: "metadata-topic", type: "input", defaultValue: "esthesis-metadata",
          props: {
            label: "The topic to read metadata data from devices",
          },
        },
        { key: "application-topic", type: "input", defaultValue: "esthesis-app",
          props: {
            label: "The topic to read platform notification data",
          },
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
