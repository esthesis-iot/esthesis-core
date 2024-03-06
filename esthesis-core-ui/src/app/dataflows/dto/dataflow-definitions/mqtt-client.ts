import {
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_KUBERNETES,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_STATUS
} from "./templates";

export const DATAFLOW_DEFINITION_MQTT_CLIENT = {
  type: "mqtt-client",
  title: "MQTT Client",
  category: "Data reader",
  description: "An MQTT client allows to fetch external messages into the platform by connecting to an MQTT broker.",
  icon: "assets/img/dataflows/mqtt.png",
  fields: [
    { fieldGroup: DATAFLOW_TEMPLATE_STATUS },
    { fieldGroup: DATAFLOW_TEMPLATE_MAIN },
    { key: "config.mqtt-broker", wrappers: ["section"], props: {label: "MQTT Broker"},
      fieldGroup: [
        { key: "cluster-url", type: "input", defaultValue: "tcp://mosquitto:1883",
          props: {
            required: true, label: "Cluster URL", placeholder: "Protocol, IP address, port",
            hintStart: "e.g. tcp://mosquitto:1883, ssl://mosquitto:8883",
          }
        },
        { key: "keep-alive-interval", type: "input", defaultValue: 30,
          props: {required: true, label: "Keep alive interval",
          hintStart: "The interval at which to send MQTT PINGREQ packets to the broker, in seconds."} },
        { key: "cert", type: "input", props: {required: false, label: "Client certificate",
          hintStart: "Specify a filesystem location, e.g. /etc/esthesis/secrets/client.crt"} },
        { key: "key", type: "input", props: {required: false, label: "Client private key",
          hintStart: "Specify a filesystem location, e.g. /etc/esthesis/secrets/client.key"} },
        { key: "ca", type: "input", props: {required: false, label: "Certificate Authority certificate",
          hintStart: "Specify a filesystem location, e.g. /etc/esthesis/secrets/ca.cert"} },
      ],
    },
    { key: "config.mqtt", wrappers: ["section"], props: {label: "MQTT Topics"},
      fieldGroup: [
        { key: "ping-topic", type: "input", defaultValue: "esthesis/ping",
          props: {
            label: "The topic receiving heartbeat messages from devices",
          },
        },
        { key: "telemetry-topic", type: "input", defaultValue: "esthesis/telemetry",
          props: {
            label: "The topic receiving telemetry data from devices",
          },
        },
        { key: "metadata-topic", type: "input", defaultValue: "esthesis/metadata",
          props: {
            label: "The topic receiving metadata data from devices",
          },
        },
        { key: "command-request-topic", type: "input", defaultValue: "esthesis/command/request",
          props: {
            label: "The topic in which command requests are submitted by the platform to be executed by the devices",
          },
        },
        { key: "command-reply-topic", type: "input", defaultValue: "esthesis/command/reply",
          props: {
            label: "The topic in which command replies are submitted back from devices",
          },
        },
      ],
    },
    { key: "config.kafka", wrappers: ["section"], props: {label: "Kafka Broker"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        { key: "ping-topic", type: "input", defaultValue: "esthesis-ping",
          props: {
            required: true, label: "The topic receiving heartbeat messages from devices",
          },
        },
        { key: "telemetry-topic", type: "input", defaultValue: "esthesis-telemetry",
          props: {
            required: true, label: "The topic receiving telemetry data from devices",
          },
        },
        { key: "metadata-topic", type: "input", defaultValue: "esthesis-metadata",
          props: {
            required: true, label: "The topic receiving metadata data from devices",
          },
        },
        { key: "command-request-topic", type: "input", defaultValue: "esthesis-command-request",
          props: {
            required: true, label: "The topic in which command requests are submitted by the platform to be executed by the devices",
          },
        },
        { key: "command-reply-topic", type: "input", defaultValue: "esthesis-command-reply",
          props: {
            required: true, label: "The topic in which command replies are submitted back from devices",
          }
        }
      ]
    },
    { key: "config.kubernetes", wrappers: ["section"], props: {label: "Kubernetes"},
      fieldGroup: DATAFLOW_TEMPLATE_KUBERNETES
    },
    { key: "config.logging", wrappers: ["section"], props: {label: "Logging"},
      fieldGroup: DATAFLOW_TEMPLATE_LOGGING
    }
  ]
};
