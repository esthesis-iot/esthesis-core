const TEMPLATE_MAIN = [
  {key: "name", type: "input", props: {required: true, type: "text", label: "Name"}},
  {key: "description", type: "input", props: {required: false, type: "text", label: "Description"}}
];

const TEMPLATE_TAGS = [
  {
    key: "config.tags", type: "select",
    props: {
      label: "Tags",
      hintStart: "Select the tags associated with this MQTT broker",
      required: false, multiple: true,
      options: []
    }
  }
];

const TEMPLATE_KAFKA = [{
  key: "config.kafka", wrappers: ["section"],
  props: {label: "Kafka Brokers"},
  fieldGroup: [
    {key: "url", type: "input", props: {required: true, type: "text", label: "URL"}}
  ]
}];

const TEMPLATE_KUBERNETES = [{
  key: "kubernetes", wrappers: ["section"],
  props: {label: "Kubernetes Scheduling"},
  fieldGroup: [
    {key: "namespace", type: "select", props: {required: true, type: "text", label: "Namespace"}},
    {
      key: "docker", type: "select",
      props: {
        label: "Select the version of this dataflow to deploy", required: true, multiple: false,
        options: []
      }
    },
    {
      key: "pods-min", type: "slider", defaultValue: 1,
      props: {label: "Minimum pods", required: true, min: 1, max: 100, thumbLabel: true},
    },
    {
      key: "pods-max", type: "slider", defaultValue: 1,
      props: {label: "Maximum pods", required: true, min: 1, max: 100, thumbLabel: true},
    },
  ]
}];

const TEMPLATE_STATUS = [
  {key: "status", type: "toggle", defaultValue: true, props: {label: "Active", required: true}},
];

export const dataflows = [
  {
    type: "mqtt-client",
    title: "MQTT Client",
    category: "Data reader",
    description: "An MQTT client allows to fetch external messages into the platform by connecting to an MQTT broker.",
    icon: "assets/img/dataflows/mqtt.png",
    fields: [
      ...TEMPLATE_STATUS,
      ...TEMPLATE_MAIN,
      {
        key: "config.mqtt-broker", wrappers: ["section"],
        props: {label: "MQTT Broker"},
        fieldGroup: [
          {
            key: "url", type: "input",
            props: {
              required: true, type: "text", label: "URL", placeholder: "Protocol, IP address, port",
              hintStart: "ex: tcp://mqtt.server:1883, ssl://mqtt.server:8883",
            },
          },
          ...TEMPLATE_TAGS
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
              required: true, type: "text", label: "The topic receiving heartbeat messages from devices",
            },
          },
          {
            key: "telemetry", type: "input",
            defaultValue: "esthesis/telemetry",
            props: {
              required: true, type: "text", label: "The topic receiving telemetry data from devices",
            },
          },
          {
            key: "metadata", type: "input",
            defaultValue: "esthesis/metadata",
            props: {
              required: true, type: "text", label: "The topic receiving metadata data from devices",
            },
          },
          {
            key: "control-request", type: "input",
            defaultValue: "esthesis/control/request",
            props: {
              required: true, type: "text", label: "The topic in which control requests are submitted by the platform to be executed by the devices",
            },
          },
          {
            key: "control-reply", type: "input",
            defaultValue: "esthesis/control/reply",
            props: {
              required: true, type: "text", label: "The topic in which control replies are submitted back from devices",
            },
          },
        ],
      },
      ...TEMPLATE_KAFKA,
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
            key: "control-request", type: "input",
            defaultValue: "esthesis-control-request",
            props: {
              required: true, type: "text", label: "The topic in which control requests are submitted by the platform to be executed by the devices",
            },
          },
          {
            key: "control-reply", type: "input",
            defaultValue: "esthesis-control-reply",
            props: {
              required: true, type: "text", label: "The topic in which control replies are submitted back from devices",
            },
          },
        ],
      },
      ...TEMPLATE_KUBERNETES
    ]
  }
];
