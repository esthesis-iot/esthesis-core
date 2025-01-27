# Fiware Orion gateway DFL

The Fiware Orion gateway DFL is responsible for updating the Fiware Orion Context Broker with incoming telemetry
and metadata values. It obtains values from Kafka, after they have been passed all possible processing steps.
Data is classified on categories, with each category having one or more measurements. The DFL can also create new
devices in Orion, if they are not yet known. It also provides detailed customisation options on how devices and
data are synchronised with Orion. The DFL supports authentication with Orion, and can be configured to use
different authentication methods.

## Creating devices in Orion

To be able to push devices to Orion, the device should already be registered in Orion. The DFL can create the device
in Orion automatically when configured so. Each device is registered with a name after its hardware ID, however you
can set up a device attribute to act as the Orion device name. Similarly, a default device type named 'Device' is used,
and that can also be customised in the settings of the DFL.

You can have the DFL automatically trying to register in Orion all new devices in esthesis, or specify manually which
devices should be registered using a boolean attribute set for devices.

## Customising data synchronisation
By default, the DFL will push data to Orion as [TBC]. You can customise the data synchronisation by specifying
[TBC]. For example [TBC].

## Authentication
[TBC]

## Inputs

- Gateway configuration: The DFL requires the connection details to the Fiware Orion Context Broker, including the URL,
  the service, the service path, and the service port. You can also specify the authentication method and credentials,
  as well as customising which devices get registered in Orion, how data is pushed, etc.
- Redis: The DFL requires the connection details to the Redis cache. 
- Kafka: The DFL obtains telemetry and metadata from Kafka. It requires Kafka connection details, as well as the topic
  names from which to read metadata and telemetry. You may leave empty either of those, if you do not need to process
  telemetry or metadata respectively.
- Kubernetes: The DFL is deployed as a Kubernetes deployment and requires the Kubernetes connection details.
- Concurrency: You can set the number of messages the DFL can locally queue for processing, the polling frequency, as
  well as the number of parallel execution processing threads.
- Logging: You can specify the logging level for the esthesis components in this DFL, as well as the general logging
  level.

## Outputs

The DFL updates a Fiware Orion Context Broker with incoming telemetry and metadata values.