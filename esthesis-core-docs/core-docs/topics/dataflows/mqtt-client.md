# MQTT client DFL

The MQTT client DFL is responsible to fetch data from an MQTT server and pass it to Kafka to be further processed
by other DFLs. The DFL expects data to be in the [esthesis Line Protocol (eLP)](esthesis-line-protocol.md) and
it converts them to [esthesis AVRO messages](avro-support.md) before pushing them to Kafka.

## Inputs

- MQTT Broker: The DFL requires the connection details to the MQTT server. It supports certificate-based authentication
as well as self-signed certificates.
- MQTT Topics: You can specify the name of the MQTT topics used for ping, telemetry, metadata, command-request, and
command-reply messages.
- Kafka: The DFL obtains telemetry and metadata from Kafka. It requires Kafka connection details, as well as the topic
  names from which to read metadata and telemetry. You may leave empty either of those, if you do not need to process
  telemetry or metadata respectively.
- Kubernetes: The DFL is deployed as a Kubernetes deployment and requires the Kubernetes connection details.
- Concurrency: You can set the number of messages the DFL can locally queue for processing, the polling frequency, as
  well as the number of parallel execution processing threads.
- Logging: You can specify the logging level for the esthesis components in this DFL, as well as the general logging
  level.