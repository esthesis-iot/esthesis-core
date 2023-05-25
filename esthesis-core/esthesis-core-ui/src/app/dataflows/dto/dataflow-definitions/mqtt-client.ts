import {
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS,
  DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES
} from "./templates";

export const DATAFLOW_DEFINITION_MQTT_CLIENT = {
  type: "mqtt-client",
  title: "MQTT Client",
  category: "Data reader",
  description: "An MQTT client allows to fetch external messages into the platform by connecting to an MQTT broker.",
  icon: "assets/img/dataflows/mqtt.png",
  fields: [
    ...DATAFLOW_TEMPLATE_STATUS,
    ...DATAFLOW_TEMPLATE_MAIN,
    {
      key: "config.mqtt-broker", wrappers: ["section"],
      props: {label: "MQTT Broker"},
      fieldGroup: [
        {
          key: "cluster-url", type: "input", defaultValue: "tcp://mosquitto:1883",
          props: {
            required: true, label: "Cluster URL", placeholder: "Protocol, IP address, port",
            hintStart: "ex: tcp://mosquitto:1883, ssl://mosquitto:8883",
          }
        }
      ],
    },
    {
      key: "config.mqtt-topic", wrappers: ["section"],
      props: {label: "MQTT Topics"},
      fieldGroup: [
        {
          key: "ping", type: "input",
          defaultValue: "esthesis/ping",
          props: {
            label: "The topic receiving heartbeat messages from devices",
          },
        },
        {
          key: "telemetry", type: "input",
          defaultValue: "esthesis/telemetry",
          props: {
            label: "The topic receiving telemetry data from devices",
          },
        },
        {
          key: "metadata", type: "input",
          defaultValue: "esthesis/metadata",
          props: {
            label: "The topic receiving metadata data from devices",
          },
        },
        {
          key: "command-request", type: "input",
          defaultValue: "esthesis/command/request",
          props: {
            label: "The topic in which command requests are submitted by the platform to be executed by the devices",
          },
        },
        {
          key: "command-reply", type: "input",
          defaultValue: "esthesis/command/reply",
          props: {
            label: "The topic in which command replies are submitted back from devices",
          },
        },
      ],
    },
    {
      key: "config.kafka", wrappers: ["section"],
      props: {label: "Kafka Broker"},
      fieldGroup: [
        {
          key: "cluster-url", type: "input", defaultValue: "kafka-headless:9094",
          props: {
            required: true,
            type: "text",
            label: "Cluster URL",
            hintStart: "e.g. kafka-headless:9094"
          }
        }
      ]
    },
    {
      key: "config.kafka-topic", wrappers: ["section"],
      props: {label: "Kafka Topics"},
      fieldGroup: [
        {
          key: "ping", type: "input",
          defaultValue: "esthesis-ping",
          props: {
            required: true, label: "The topic receiving heartbeat messages from devices",
          },
        },
        {
          key: "telemetry", type: "input",
          defaultValue: "esthesis-telemetry",
          props: {
            required: true, label: "The topic receiving telemetry data from devices",
          },
        },
        {
          key: "metadata", type: "input",
          defaultValue: "esthesis-metadata",
          props: {
            required: true, label: "The topic receiving metadata data from devices",
          },
        },
        {
          key: "command-request", type: "input",
          defaultValue: "esthesis-command-request",
          props: {
            required: true, label: "The topic in which command requests are submitted by the platform to be executed by the devices",
          },
        },
        {
          key: "command-reply", type: "input",
          defaultValue: "esthesis-command-reply",
          props: {
            required: true, label: "The topic in which command replies are submitted back from devices",
          },
        },
      ],
    },
    ...DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES,
    ...DATAFLOW_TEMPLATE_LOGGING
  ]
};
