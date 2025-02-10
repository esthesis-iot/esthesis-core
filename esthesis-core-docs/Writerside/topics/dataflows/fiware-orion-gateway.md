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
By default, the DFL pushes data to Orion as **NGSI-v2** valid entities. These entities are also designed to be
compatible with the **NGSI-LD** standard. Also, any new data available in Kafka will be immediately sent to Orion.
However, the DFL provides several ways to customise how data is synchronised with Orion:

---

### 1. Specifying Attribute Names

You can define a list of attribute names to be synchronised. Any measurement not included in this list will be ignored
and will not be sent to Orion.

For example:
If your esthesis device generates multiple measurements such as `battery`, `temperature`, `co2`, `memoryUsage`, etc.,
but you only want to synchronise the `temperature` and `co2` attributes, you can specify the attribute names in the
configuration as follows:
`temperature,co2`

This ensures only the specified attributes are sent to Orion.

---

### 2. Configuring Time Intervals

The DFL allows you to configure a **minimum time interval (in seconds)** for forwarding data from Kafka to Orion.
This prevents data from being sent every time new data arrives and instead sends it periodically.

For example:
Setting the configuration value to `900` (15 minutes) ensures that the same measurement will only be sent at least once
every 15 minutes. Any values received for that measurement during the interval will not be sent to Orion until the
interval elapses.

> **Important**: To ensure this configuration works reliably, make sure there is only one Kafka message consumer configured
> for this DFL instance. Multiple consumers could disrupt the interval-based forwarding logic.

> **Important²**: This interval rule will not be triggered by past data of a measurement that has already been sent in
> real time. In other words, for historical data ingestion, any required interval should be prepared beforehand.

---

### 3. Creating Custom JSON

If the default NGSI-v2 structure does not meet your requirements, you can define a fully customized JSON payload
to be sent to Orion. This can be configured globally or per device.

#### Option 1: Global Custom JSON Format (Applies to All Measurements)
1. Define a global JSON format using the custom entity JSON configuration input.
2. Set its value as a valid [Qute template](https://quarkus.io/guides/qute) expression that outputs a valid Orion Fiware JSON.
3. Use the following variables in the template:
	- `{category}`
	- `{timestamp}`
	- `{hardwareId}`
	- `{measurementName}`
	- `{measurementValue}`

This approach ensures that all measurements from all esthesis agents follow the same JSON structure.

#### Option 2: Per-Device Custom JSON Format (Overrides Global Format)
1. Create an attribute in your esthesis device, e.g., `orion-custom-json`.
2. Set the attribute value as a valid [Qute template](https://quarkus.io/guides/qute) expression that outputs a valid Orion Fiware JSON.
3. Use the following variables in the template:
	- `{category}`
	- `{timestamp}`
	- `{hardwareId}`
	- `{measurementName}`
	- `{measurementValue}`
4. Put your created attribute name in the custom entity JSON configuration, e.g., `orion-custom-json`.
5. If this attribute is defined, it takes precedence over the global JSON format.

This setup allows for flexibility:
- If a device-specific template is set, it will be used for that given device.
- If not, the global template will be used.

#### Example of Qute Template expression for a custom JSON Format:
```json
[
  {
    "id": "{measurementName}",
    "type": "{category}-custom-type",
    "temperature": {
      "type": "Property",
      "value": "{measurementValue}",
      "unitCode": "CEL",
      "observedAt": "{timestamp}"
    }
  }
]
```

## Authentication
Currently, two authentication methods are supported:

1. **No Authentication**
	 To access to Orion instance that doesn't require authentication.

2. **Keyrock OAuth2 Authentication**
	 When using Keyrock for authentication, the following information must be configured:

	- **Authentication URL**:
		The URL of the Keyrock Identity Provider to be used for authentication.

	- **Credential Token**:
		The token to be used for authenticating requests. This is typically obtained from the Keyrock platform.

	- **Username**:
		The username of the account that has access to the target Orion service.

	- **Password**:
		The password for the specified username.

	- **Grant Type**:
		The OAuth2 grant type to be used for authentication, e,g., `password`
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
