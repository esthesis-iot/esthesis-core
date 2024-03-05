// *************************************************************************************************
// Main
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_MAIN = [
  { key: "name", type: "input", props: {required: true, label: "Name"} },
  { key: "description", type: "input", props: {required: false, label: "Description"} }
];

// *************************************************************************************************
// Status
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_STATUS = [
  { key: "status", type: "toggle", defaultValue: true, props: {label: "Active"} }
];

// *************************************************************************************************
// MongoDB
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_MONGODB = [
    { key: "url", type: "input", defaultValue: "mongodb://mongodb-headless:27017",
      props: {label: "URL", hintStart: "e.g. mongodb://mongodb-headless:27017"} },
    { key: "name", type: "input", defaultValue: "esthesiscore",
      props: {label: "Database name"} },
    { key: "username", type: "input", defaultValue: "esthesis-system",
      props: {label: "Username"} },
    { key: "password", type: "input", defaultValue: "esthesis-system",
      props: {label: "Password", type: "password"} }
  ]
;

// *************************************************************************************************
// Concurrency
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_CONCURRENCY = [
  { key: "queue-size", type: "input", defaultValue: "1000",
    props: { required: true, label: "Number of messages in the processing queue"} },
  { key: "poll-timeout", type: "input", defaultValue: "1000", props: {
    required: true, label: "How often to poll the queue for new messages (in msec)"} },
  { key: "consumers", type: "input", defaultValue: "10", props: {
    required: true, label: "Number of concurrent message consumers"} }
];

// *************************************************************************************************
// Kafka
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_KAFKA = [
  { key: "cluster-url", type: "input", defaultValue: "kafka:9092",
    props: {required: true, label: "Cluster URL", hintStart: "e.g. kafka:9092"} },
  { key: "security-protocol", type: "input", defaultValue: "SASL_PLAINTEXT",
    props: {required: false, label: "Security protocol", hintStart: "e.g. SASL_PLAINTEXT"} },
  { key: "sasl-mechanism", type: "input", defaultValue: "SCRAM-SHA-512",
    props: {required: false, label: "SASL mechanism", hintStart: "e.g. SCRAM-SHA-512"} },
  { key: "jaas-config", type: "input", defaultValue: "org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;",
    props: {required: false, label: "JAAS configuration", type: "password",
      hintStart: "e.g. org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;"} },
];

// *************************************************************************************************
// Kubernetes
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_KUBERNETES = [
  { key: "namespace", type: "select", props: {required: true, label: "Namespace"} },
  { key: "cpu-request", type: "input", defaultValue: "100m", props: {required: true, label: "CPU Request"} },
  { key: "cpu-limit", type: "input", defaultValue: "1", props: {required: true, label: "CPU Limit"} },
  { key: "container-image-version", type: "select",
    props: {
      label: "Select the version of this dataflow to deploy", required: true, multiple: false,
      options: []
    }
  },
  { key: "registry", type: "input", defaultValue: "",
    props: {
      required: false, label: "Custom image registry",
      hintStart: "e.g. 10.111.22.22:32000/project"} },
  { key: "pods-min", type: "input", defaultValue: "1",
    props: {label: "Minimum pods", required: true} },
  { key: "pods-max", type: "input", defaultValue: "1",
    props: {label: "Maximum pods", required: true} },
  { key: "env", type: "textarea", props: {label: "Environment variables", autosize: true, autosizeMinRows: 1} },
  { key: "secrets", type: "repeat-secret",
    props: { addText: "Add secret" },
    fieldArray: {
      fieldGroup: [
        { key: "name", type: "input", props: {label: "Secret key name", required: true,
            className: "flex-row", hintStart: "Mount under /etc/esthesis/secrets/<name>"} },
        { key: "content", type: "textarea", props: {label: "Content", required: true,
            className: "flex-row", autosize: true, autosizeMinRows: 1, autosizeMaxRows: 10} }
      ]
    }
  }
];

// *************************************************************************************************
// InfluxDB
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_INFLUXDB = [
  { key: "url", type: "input", defaultValue: "http://influxdb:8086",
    props: {label: "URL"} },
  { key: "token", type: "input",
    props: {label: "Access token", type: "password", required: true} },
  { key: "org", type: "input", defaultValue: "esthesis",
    props: {label: "Organisation name"} },
  { key: "bucket", type: "input", defaultValue: "esthesis",
    props: {label: "Storage bucket name"} }
];

// *************************************************************************************************
// Redis
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_REDIS = [
  { key: "url", type: "input", defaultValue: "redis://:esthesis-system@redis-master:6379/0",
    props: {label: "The url of the Redis store", required: true, hintStart: "e.g. redis://username:password@server:port/database. Password should be URL Encoded, if it contains special characters."} },
  { key: "max-size", type: "input", defaultValue: "1024",
    props: {
      label: "The maximum value size (in bytes) eligible for caching", required: true,
      hintStart: "This value pertains to device measurements caching, provisioning packages will be cached regardless of size."
    } },
  { key: "ttl", type: "input", defaultValue: "0",
    props: {
      label: "Time to live (in minutes), set to 0 to never automatically expire cached entries.",
      required: true, hintStart: "This value pertains to device measurements caching, provisioning packages will be cached forever."
    }
  }
];

// *************************************************************************************************
// Logging
// *************************************************************************************************
export const DATAFLOW_TEMPLATE_LOGGING = [
  { key: "common", type: "select", defaultValue: "INFO",
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
  { key: "esthesis", type: "select", defaultValue: "INFO",
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
];
