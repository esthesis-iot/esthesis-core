export const DATAFLOW_TEMPLATE_MAIN = [
  {key: "name", type: "input", props: {required: true, type: "text", label: "Name"}},
  {key: "description", type: "input", props: {required: false, type: "text", label: "Description"}}
];

export const DATAFLOW_TEMPLATE_TAGS = [
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

export const DATAFLOW_TEMPLATE_WRAPPED_CONCURRENCY = [{
  key: "config", wrappers: ["section"],
  props: {label: "Concurrency"},
  fieldGroup: [
    {
      key: "queue-size", type: "input", defaultValue: "1000", props: {
        required: true, type: "text", label: "Number of messages in the processing queue",
      }
    }, {
      key: "poll-timeout", type: "input", defaultValue: "500", props: {
        required: true, type: "text", label: "How often to poll the queue for new messages (in msec)",
      }
    }, {
      key: "consumers", type: "input", defaultValue: "10", props: {
        required: true, type: "text", label: "Number of concurrent message consumers",
      }
    },
  ]
}];

export const DATAFLOW_TEMPLATE_KAFKA = [
  {
    key: "cluster-url", type: "input", defaultValue: "kafka:9092",
    props: {required: true, type: "text", label: "Cluster URL"}
  },
];

export const DATAFLOW_TEMPLATE_WRAPPED_KUBERNETES = [{
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

export const DATAFLOW_TEMPLATE_STATUS = [
  {key: "status", type: "toggle", defaultValue: true, props: {label: "Active"}},
];

export const DATAFLOW_TEMPLATE_LOGGING = [{
  key: "config.logging", wrappers: ["section"],
  props: {label: "Logging configuration"},
  fieldGroup: [
    {
      key: "common", type: "select", defaultValue: "INFO",
      props: {
        label: "Select the global logging level for this dataflow", required: true, multiple: false,
        options: [
          {value: "OFF", label: "Off"},
          {value: "FATAL", label: "Fatal"},
          {value: "ERROR", label: "Error"},
          {value: "WARN", label: "Warn"},
          {value: "INFO", label: "Info"},
          {value: "DEBUG", label: "Debug"},
          {value: "TRACE", label: "Trace"},
          {value: "ALL", label: "All"},
        ]
      }
    },
    {
      key: "esthesis", type: "select", defaultValue: "INFO",
      props: {
        label: "Select the esthesis logging level for this dataflow", required: true, multiple: false,
        options: [
          {value: "OFF", label: "Off"},
          {value: "FATAL", label: "Fatal"},
          {value: "ERROR", label: "Error"},
          {value: "WARN", label: "Warn"},
          {value: "INFO", label: "Info"},
          {value: "DEBUG", label: "Debug"},
          {value: "TRACE", label: "Trace"},
          {value: "ALL", label: "All"},
        ]
      }
    }
  ]
}];
