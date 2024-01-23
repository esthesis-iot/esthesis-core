import {
  DATAFLOW_TEMPLATE_CONCURRENCY,
  DATAFLOW_TEMPLATE_KAFKA,
  DATAFLOW_TEMPLATE_KUBERNETES,
  DATAFLOW_TEMPLATE_LOGGING,
  DATAFLOW_TEMPLATE_MAIN,
  DATAFLOW_TEMPLATE_MONGODB,
  DATAFLOW_TEMPLATE_STATUS
} from "./templates";

export const DATAFLOW_DEFINITION_COMMAND_REPLY_UPDATER = {
  type: "command-reply-updater",
  title: "Command Reply updater",
  category: "Data update",
  description: "A component handling command reply messages from devices updating the esthesis database with each reply received.",
  icon: "assets/img/dataflows/command-reply.png",
  fields: [
    { fieldGroup: DATAFLOW_TEMPLATE_STATUS },
    { fieldGroup: DATAFLOW_TEMPLATE_MAIN },
    { key: "config.kafka", wrappers: ["section"], props: {label: "Kafka"},
      fieldGroup: [
        ...DATAFLOW_TEMPLATE_KAFKA,
        { key: "consumer-group", type: "input", defaultValue: "dfl-command-reply-updater",
          props: {label: "Kafka consumer group"}
        },
        { key: "command-reply-topic", type: "input", defaultValue: "esthesis-command-reply",
          props: {label: "Kafka topic to read command reply messages from", required: true}
        }
      ]
    },
    { key: "config.esthesis-db", wrappers: ["section"], props: {label: "Kafka"},
      fieldGroup: DATAFLOW_TEMPLATE_MONGODB },
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
