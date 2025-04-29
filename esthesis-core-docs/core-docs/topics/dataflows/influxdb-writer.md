# InfluxDB Writer dataflow

The InfluxDB Writer DFL is responsible to update an InfluxDB database with incoming telemetry, and possibly metadata,
values. It obtains values from Kafka, after they have been passed all possible processing steps. Data is classified
on categories, with each category having one or more measurements. Categories and measurements are created automatically
once new data is discovered.

## Data types

A feature of this DFL to be aware of is its approach towards data-type discovery. When new incoming data is encountered
and a measurement is created in InfluxDB, this measurement is initiated in InfluxDB with a specific data type. InfluxDB
will try to automatically determine the data type of the field, however this might not always be what you want. For
example, consider the case of your sensor values being decimal numbers. If for any reason your devices send non-decimal
values of your sensors without a decimal part (i.e. ".0"), then InfluxDB might consider this field to be an integer
type instead of a float type. This can lead to issues when you later on try to persist decimal values, as InfluxDB will
reject these values.

To avoid such issues, the [esthesis Line Protocol (eLP)](esthesis-line-protocol.md) allows you to specify the data type
of a value by providing hints as suffixes. When possible, we strongly recommend to use this mechanism to avoid
surprises. When a data type is explicitly specified in eLP, this DFL will explicitly create the requested type in
InfluxDB thus guaranteeing your future values will be properly processed.

## Inputs

- InfluxDB: The DFL requires the connection details to the InfluxDB database, including the URL, an access token, and
  the number of the bucket to write to.
- Kafka: The DFL obtains telemetry and metadata from Kafka. It requires Kafka connection details, as well as the topic
  names from which to read metadata and telemetry. You may leave empty either of those, if you do not need to process
  telemetry or metadata respectively.
- Kubernetes: The DFL is deployed as a Kubernetes deployment and requires the Kubernetes connection details.
- Concurrency: You can set the number of messages the DFL can locally queue for processing, the polling frequency, as
  well as the number of parallel execution processing threads.
- Logging: You can specify the logging level for the esthesis components in this DFL, as well as the general logging
  level.

## Outputs

The DFL updates an InfluxDB database with incoming telemetry and metadata values.