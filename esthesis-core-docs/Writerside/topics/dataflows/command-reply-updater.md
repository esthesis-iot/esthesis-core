# Command Reply Updater DFL

The Command Reply Updater DFL is responsible to update command replies sent by devices. esthesis CORE dispatches all
commands to devices via MQTT, by posting the command to the device's command topic. The device then obtains the message
from the MQTT server, processes it, and sends a reply to the command reply topic. The Command Reply updater DFL is
then picking up command-reply MQTT messages and updates the command reply document collection in the esthesis CORE
database.

This DFL is largely an internal component of esthesis CORE, necessary to be deployed to ensure that the
command reply document collection is updated with the latest information. However, if it suits your use cases, you may
deploy additional instances of the FDL, so that you also update replies in a separate database you maintain.

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

The DFL updates the esthesis CORE database document collection `CommandReply`. 