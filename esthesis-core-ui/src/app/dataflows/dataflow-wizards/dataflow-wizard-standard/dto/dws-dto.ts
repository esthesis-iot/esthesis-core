import {
  DATAFLOW_TEMPLATE_INFLUXDB,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_KUBERNETES,
  DATAFLOW_TEMPLATE_MONGODB,
  DATAFLOW_TEMPLATE_REDIS,
} from "../../../dto/dataflow-definitions/templates";

export const DATAFLOW_WIZARD_STANDARD = {
  type: "influxdb-writer",
  title: "InfluxDB Writer",
  category: "Data writer",
  description: "A component handling telemetry and metadata messages from devices, updating an InfluxDB database.",
  icon: "assets/img/dataflows/influxdb.png",
  fields: [
    { key: "config.kafka", wrappers: ["section"],
      props: {label: "Kafka"},
      fieldGroup: DATAFLOW_TEMPLATE_KAFKA },
    { key: "config.esthesis-db", wrappers: ["section"],
      props: {label: "esthesis CORE database"},
      fieldGroup: DATAFLOW_TEMPLATE_MONGODB },
    { key: "config.influx", wrappers: ["section"], props: {label: "InfuxDB"},
      fieldGroup: DATAFLOW_TEMPLATE_INFLUXDB },
    { key: "config.redis", wrappers: ["section"], props: {label: "Redis"},
      fieldGroup: DATAFLOW_TEMPLATE_REDIS },
    { key: "config.mqtt-broker", wrappers: ["section"],
      props: {label: "MQTT broker"},
      fieldGroup: [
        { key: "cluster-url", type: "input", defaultValue: "tcp://mosquitto:1883",
          props: {
            required: true, label: "Cluster URL", placeholder: "Protocol, IP address, port",
            hintStart: "e.g. tcp://mosquitto:1883, ssl://mosquitto:8883"} }
      ]
    },
    { key: "config.kubernetes", wrappers: ["section"], props: {label: "Kubernetes"},
      fieldGroup: DATAFLOW_TEMPLATE_KUBERNETES }
  ]
}
