# Ping Updater dataflow

The Ping Updater DFL is responsible to update ping/keep-alive messages sent from devices. This is the information
you see under device "last seen" indicators in esthesis CORE. Keep-alive, or ping, messages are obtained from Kafka
and processed through this plugin to update the esthesis CORE database.

## Inputs

- Kafka: The DFL obtains update from Kafka. It requires Kafka connection details, as well as the topic
  name from which to read the updates.
- esthesis CORE database: The DFL needs to update the command reply document collection in the esthesis CORE database
  with command replies. It requires the database connection details.
- Kubernetes: The DFL is deployed as a Kubernetes deployment and requires the Kubernetes connection details.
- Concurrency: You can set the number of messages the DFL can locally queue for processing, the polling frequency, as
  well as the number of parallel execution processing threads.
- Logging: You can specify the logging level for the esthesis components in this DFL, as well as the general logging
  level.

## Outputs

The DFL updates the esthesis CORE database document collection `Device`.