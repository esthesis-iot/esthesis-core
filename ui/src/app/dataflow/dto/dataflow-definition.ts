const TEMPLATE_MAIN = [
  {key: "name", type: "input", props: {required: true, type: "text", label: "Name"}},
  {key: "description", type: "input", props: {required: false, type: "text", label: "Description"}}
];

const TEMPLATE_TAGS = [
  {
    key: "tags", type: "select",
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
    {
      key: "cluster-url", type: "input", defaultValue: "esthesis-kafka:9092", props: {
        required: true, type: "text", label: "Cluster URL",
      }
    }
  ]
}];

const TEMPLATE_KUBERNETES = [{
  key: "kubernetes", wrappers: ["section"],
  props: {label: "Kubernetes Scheduling"},
  fieldGroup: [
    {key: "namespace", type: "select", props: {required: true, type: "text", label: "Namespace"}},
    {key: "cpu-request", type: "input", defaultValue: "250m", props: {required: true, type: "text", label: "CPU Request"}},
    {key: "cpu-limit", type: "input", defaultValue: "1", props: {required: true, type: "text", label: "CPU Limit"}},
    {
      key: "docker", type: "select",
      props: {
        label: "Select the version of this dataflow to deploy", required: true, multiple: false,
        options: []
      }
    },
    {
      key: "pods-min", type: "input", defaultValue: "1",
      props: {label: "Minimum pods", required: true},
    },
    {
      key: "pods-max", type: "input", defaultValue: "10",
      props: {label: "Maximum pods", required: true},
    },
  ]
}];

const TEMPLATE_STATUS = [
  {key: "status", type: "toggle", defaultValue: true, props: {label: "Active"}},
];

export const dataflows = [
  {
    type: "ping-updater",
    title: "Ping updater",
    category: "Data update",
    description: "A component handling ping messages from devices, updating their 'last seen' entry in  esthesis database.",
    icon: "assets/img/dataflows/radar.png",
    fields: [
      ...TEMPLATE_STATUS,
      ...TEMPLATE_MAIN,
      {
        key: "config", wrappers: ["section"],
        props: {label: "Kafka"},
        fieldGroup: [
          {
            key: "kafka-cluster-url", type: "input", defaultValue: "esthesis-kafka-headless:9092",
            props: {required: true, type: "text", label: "Cluster URL"}
          },
          {
            key: "kafka-ping-topic", type: "input", defaultValue: "esthesis-ping",
            props: {label: "Kafka topic to read ping messages from", required: true}
          }, {
            key: "kafka-group", type: "input", defaultValue: "esthesis-ping-updater",
            sprops: {label: "Kafka consumer group"}
          },
        ]
      },
      {
        key: "config", wrappers: ["section"],
        props: {label: "Esthesis database"},
        fieldGroup: [
          {
            key: "esthesis-db-url", type: "input", defaultValue: "mongodb://esthesis-mongodb:27017",
            props: {label: "URL"}
          }, {
            key: "esthesis-db-name", type: "input", defaultValue: "esthesis",
            props: {label: "Database name"}
          }, {
            key: "esthesis-db-username", type: "input", defaultValue: "esthesis",
            props: {label: "Username"}
          }, {
            key: "esthesis-db-password", type: "input", defaultValue: "esthesis",
            props: {label: "Password", type: "password"},
          },
        ]
      },
      ...TEMPLATE_KUBERNETES
    ]
  },
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
  },
  {
    type: "influxdb-writer",
    title: "InfluxDB Writer",
    category: "Data writer",
    description: "A component handling telemetry and metadata messages from devices, updating an InfluxDB database.",
    icon: "assets/img/dataflows/influxdb.png",
    fields: [
      ...TEMPLATE_STATUS,
      ...TEMPLATE_MAIN,
      {
        key: "config", wrappers: ["section"],
        props: {label: "InfluxDB database"},
        fieldGroup: [
          {
            key: "influx-url", type: "input", defaultValue: "http://esthesis-influxdb:8086",
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
        key: "config", wrappers: ["section"],
        props: {label: "Kafka"},
        fieldGroup: [
          {
            key: "kafka-cluster-url", type: "input", defaultValue: "esthesis-kafka-headless:9092",
            props: {required: true, type: "text", label: "Cluster URL"}
          },
          {
            key: "kafka-telemetry-topic", type: "input", defaultValue: "esthesis-telemetry",
            props: {label: "Kafka topic to read telemetry messages from"}
          },
          {
            key: "kafka-metadata-topic", type: "input", defaultValue: "",
            props: {label: "Kafka topic to read metadata messages from"}
          },
          {
            key: "kafka-group", type: "input", defaultValue: "esthesis-dfl-influxdb-writer",
            props: {label: "Kafka consumer group"}
          },
        ]
      },
      ...TEMPLATE_KUBERNETES
    ]
  }

];
