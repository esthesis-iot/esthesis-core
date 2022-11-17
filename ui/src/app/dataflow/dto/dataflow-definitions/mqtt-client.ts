import {
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS,
  DATAFLOW_TEMPLATE_TAGS,
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
          key: "cluster-url", type: "input", defaultValue: "tcp://esthesis-rabbitmq:1883",
          props: {
            required: true, type: "text", label: "Cluster URL", placeholder: "Protocol, IP address, port",
            hintStart: "ex: tcp://esthesis-rabbitmq:1883, ssl://esthesis-rabbitmq:8883",
          }
        },
        {
          key: "advertised-url", type: "input", defaultValue: "tcp://esthesis-rabbitmq:1883",
          props: {
            required: true, type: "text", label: "Advertised URL", placeholder: "Protocol, IP address, port",
            hintStart: "ex: tcp://mqtt.project.com:1883, ssl://mqtt.project.com:8883",
          }
        },
        ...DATAFLOW_TEMPLATE_TAGS
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
            type: "text", label: "The topic receiving heartbeat messages from devices",
          },
        },
        {
          key: "telemetry", type: "input",
          defaultValue: "esthesis/telemetry",
          props: {
            type: "text", label: "The topic receiving telemetry data from devices",
          },
        },
        {
          key: "metadata", type: "input",
          defaultValue: "esthesis/metadata",
          props: {
            type: "text", label: "The topic receiving metadata data from devices",
          },
        },
        {
          key: "command-request", type: "input",
          defaultValue: "esthesis/command/request",
          props: {
            type: "text", label: "The topic in which command requests are submitted by the platform to be executed by the devices",
          },
        },
        {
          key: "command-reply", type: "input",
          defaultValue: "esthesis/command/reply",
          props: {
            type: "text", label: "The topic in which command replies are submitted back from devices",
          },
        },
      ],
    },
    {
      key: "config.kafka", wrappers: ["section"],
      props: {label: "Kafka Broker"},
      fieldGroup: [
        {
          key: "cluster-url", type: "input", defaultValue: "esthesis-kafka:9092",
          props: {required: true, type: "text", label: "Cluster URL"}
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
            required: true, type: "text", label: "The topic receiving heartbeat messages from devices",
          },
        },
        {
          key: "telemetry", type: "input",
          defaultValue: "esthesis-telemetry",
          props: {
            required: true, type: "text", label: "The topic receiving telemetry data from devices",
          },
        },
        {
          key: "metadata", type: "input",
          defaultValue: "esthesis-metadata",
          props: {
            required: true, type: "text", label: "The topic receiving metadata data from devices",
          },
        },
        {
          key: "command-request", type: "input",
          defaultValue: "esthesis-command-request",
          props: {
            required: true, type: "text", label: "The topic in which command requests are submitted by the platform to be executed by the devices",
          },
        },
        {
          key: "command-reply", type: "input",
          defaultValue: "esthesis-command-reply",
          props: {
            required: true, type: "text", label: "The topic in which command replies are submitted back from devices",
          },
        },
      ],
    },
    ...DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES
  ]
};
