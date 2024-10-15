

# Configuration parameters
The esthesis device agent supports a plethora of rutime configuration parameters to match your
environment and runtime needs. Parameters can be defined either as environment variables or as
command line arguments. The following tables lists all available configuration options.

## Mandatory parameters
`HARDWARE_ID, --hardwareId`
: The unique identifier of the device.
<br/>(alphanumeric)

`REGISTRATION_URL, --registrationUrl`
: The URL of esthesis server to register this device with.
<br/>(alphanumeric/URL)

## Optional parameters
`ATTRIBUTES, --attributes`
: A comma-separated list of key-value pairs to be sent as attributes.
<br/>(alphanumeric)

`AUTO_UPDATE, --autoUpdate`
: A flag indicating whether the device should try to automatically obtain newer firmware once per day.
<br/>(boolean, **false**)

`DEMO_CATEGORY, --demoCategory`
: The category of data posted as demo data.
<br/>(alphanumeric, **demo**)

`DEMO_INTERVAL, --demoInterval`
: The frequency in which demo data is generated (in seconds).
<br/>(integer, **0**)

`ENDPOINT_HTTP, --endpointHttp`
: Whether the embedded HTTP server is enabled or not.
<br/>(boolean, **false**)

`ENDPOINT_HTTP_LISTENING_IP, --endpointHttpListeningIP`
: The IP address where the embedded HTTP server listens to.
<br/>(alphanumeric/IP address, **127.0.0.1**)

`ENDPOINT_HTTP_LISTENING_PORT, --endpointHttpListeningPort</code>`
: The port in which the embedded HTTP server listens to.
<br/>(integer, **8080**)
			
`ENDPOINT_HTTP_AUTH_USERNAME, --endpointHttpAuthUsername`
: The username to connect to the embedded HTTP endpoint. If defined, it must be sent as Basic auth together with the password.
<br/>(alphanumeric)

`ENDPOINT_HTTP_AUTH_PASSWORD, --endpointHttpAuthPassword`
: The password to connect to the embedded HTTP endpoint. If defined, it must be sent as Basic auth together with its username.
<br/>(alphanumeric)

`ENDPOINT_MQTT, --endpointMqtt`
: Whether the embedded MQTT server is enabled or not.
<br/>(boolean, **false**)

`ENDPOINT_MQTT_LISTENING_IP, --endpointMqttListeningIP`
: The IP address where the embedded MQTT server listens to.
<br/>(IP address, **127.0.0.1**)

`ENDPOINT_MQTT_LISTENING_PORT, --endpointMqttListeningPort`
: The port in which the embedded MQTT server listens to.
<br/>(integer, **1883**)

`ENDPOINT_MQTT_AUTH_USERNAME, --endpointMqttAuthUsername`
: The username to connect to the embedded MQTT endpoint.
<br/>(alphanumeric)

`ENDPOINT_MQTT_AUTH_PASSWORD, --endpointMqttAuthPassword`
: The password to connect to the embedded MQTT endpoint.
<br/>(alphanumeric)
		
`HEALTH_REPORT_INTERVAL, --healthReportInterval`
: The frequency to send health reports (in seconds).
<br/>(integer, **300**)
			
`HTTP_RETRY, --httpRetry`
: The number of seconds to wait before retrying a failed HTTP request.
<br/>(integer, **60**)
			
`HTTP_TIMEOUT, --httpTimeout`
: The number of seconds after which an HTTP call times out.
<br/>(integer, **60**)
			
`LOG_ABBREVIATION, --logAbbreviation`
: The characters length to abbreviate log messages to.
<br/>(integer, **1024**)
			
`LOG_LEVEL, --logLevel`
: The logging level to use [trace, debug, info].
<br/>(alphanumeric, **info**)

`LUA_HTTP_TELEMETRY_SCRIPT, --luaHttpTelemetryScript`
: The filesystem location of a Lua script to transform incoming payloads for telemetry data pushed via the HTTP endpoint.
<br/>(alphanumeric)
		
`LUA_HTTP_METADATA_SCRIPT, --luaHttpMetadataScript`
: The filesystem location of a Lua script to transform incoming payloads for metadata data pushed via the HTTP endpoint.
<br/>(alphanumeric)
		
`LUA_MQTT_TELEMETRY_SCRIPT, --luaMqttTelemetryScript`
: The filesystem location of a Lua script to transform incoming payloads for telemetry data pushed via the MQTT endpoint.
<br/>(alphanumeric)
		
`LUA_MQTT_METADATA_SCRIPT, --luaMqttMetadataScript`
: The filesystem location of a Lua script to transform incoming payloads for metadata data pushed via the MQTT endpoint.
<br/>(alphanumeric)

`--luaExtraMqttTelemetryTopic`
: A custom MQTT telemetry endpoint to be handled by a user-defined Lua script. The first argument of this
parameter is the name of the topic, and the second argument is the Lua script to be executed. This
parameter can be repeated to define additional topics.
<br/>(alphanumeric alphanumeric)
		
`--luaExtraMqttMetadataTopic`
: A custom MQTT metadata endpoint to be handled by a user-defined Lua script. The first argument of this
parameter is the name of the topic, and the second argument is the Lua script to be executed. This
parameter can be repeated to define additional topics.
<br/>(alphanumeric alphanumeric)

`--luaExtraHttpTelemetryEndpoint`
: A custom HTTP telemetry endpoint to be handled by a user-defined Lua script. The first argument of this
parameter is the name of the endpoint, and the second argument is the Lua script to be executed. This
parameter can be repeated to define additional topics.
<br/>(alphanumeric alphanumeric)
		
`--luaExtraHttpMetadataEndpoint`
: A custom HTTP metadata endpoint to be handled by a user-defined Lua script. The first argument of this
parameter is the name of the endpoint, and the second argument is the Lua script to be executed. This
parameter can be repeated to define additional topics.
<br/>(alphanumeric alphanumeric)
		
`MQTT_INFLIGHT_TTL_DURATION, --mqttInflightTTLDuration`
: The number of seconds that a queued inflight message should exist before being purged.
<br/>(integer, **60**)
			
`MQTT_TIMEOUT, --mqttTimeout`
: The number of seconds to wait before failing an outgoing MQTT message.
<br/>(integer, **60**)

`PAUSE_STARTUP, --pauseStartup`
: A flag indicating whether the device should start paused.
<br/>(boolean, **false**)
			
`PING_INTERVAL, --pingInterval`
: The frequency to ping esthesis CORE (in seconds)
<br/>(integer, **60**)
			
`PROPERTIES_FILE, --propertiesFile`
: The file to store the agent’s configuration.
<br/>(alphanumeric, **$HOME/.esthesis/device/esthesis.properties**)
			
`PROVISIONING_SCRIPT, --provisioningScript`
: The script used to install new provisioning packages.
<br/>(alphanumeric, **$HOME/.esthesis/device/firmware.sh**)
			
`REBOOT_SCRIPT, --rebootScript`
: The script used to reboot the device.
<br/>(alphanumeric, **$HOME/.esthesis/device/reboot.sh**)
			
`REGISTRATION_SECRET, --registrationSecret`
: If set, the registration request will include it as a header.
<br/>(alphanumeric)
		
`SECURE_PROPERTIES_FILE, --securePropertiesFile`
: The secure file to store sensitive parts of the agent's configuration.
<br/>(alphanumeric, **$HOME/.esthesis/device/secure/esthesis.properties**
			
`SECURE_PROVISIONING, --secureProvisioning`
: A flag indicating whether provisioning requests should be accompanied by a signature token.
<br/>(alphanumeric, **false**
		
`SHUTDOWN_SCRIPT, --shutdownScript`
: The script used to shut down the device.
<br/>(alphanumeric, **$HOME/.esthesis/device/shutdown.sh**)

`SIGNATURE_ALGORITHM, --signatureAlgorithm`
: The algorithm to use to produce signatures.
<br/>(alphanumeric, **SHA256WITHRSA**)
			
`SUPPORTED_COMMANDS, --supportedCommands`
: The remote commands this device supports:<br/>
e: Execute arbitrary<br/>
f: Firmware update<br/>
r: Reboot<br/>
s: Shutdown<br/>
p: Ping<br/>
h: Health report<br/><br/>
<br/>(alphanumeric, **efrsph**)

`TAGS, --tags`
: A comma-separated list of tags to associate with this device. Tag names should only contain letters, numbers, and underscore.
<br/>(alphanumeric)
		
`TEMP_DIR, --tempDir`
: The folder to temporarily store provisioning packages.
<br/>(alphanumeric, **_OS default temp directory_**)

`TOPIC_COMMAND_REQUEST, --topicCommandRequest`
: The MQTT topic to use for command request messages.
<br/>(alphanumeric, **esthesis/command/request**)
			
`TOPIC_COMMAND_REPLY, --topicCommandReply`
: The MQTT topic to use for command reply messages.
<br/>(alphanumeric, **esthesis/command/reply**)
			
`TOPIC_DEMO, --topicDemo`
: The MQTT topic to post demo data.
<br/>(alphanumeric, **esthesis/telemetry**)
			
`TOPIC_METADATA, --topicMetadata`
: The MQTT topic to use for metadata messages.
<br/>(alphanumeric, **esthesis/metadata**)
		
`TOPIC_PING, --topicPing`
: The MQTT topic to use for ping messages.
<br/>(alphanumeric, **esthesis/ping**)
			
`TOPIC_TELEMETRY, --topicTelemetry`
: The MQTT topic to use for telemetry messages.
<br/>(alphanumeric, **esthesis/telemetry**)
			
`VERSION_FILE, --versionFile`
: A file with a single line of text depicting the current version of the firmware running on the device.
<br/>(alphanumeric, **$HOME/.esthesis/device/version**)

`VERSION_REPORT_TOPIC, --versionReportTopic`
: The MQTT topic to report the firmware version.
<br/>(alphanumeric, **esthesis/metadata**)

`VERSION_REPORT, --versionReport`
: Report the version number available in the specified version file once during boot.
<br/>(alphanumeric, **false**

`TLS_VERIFICATION, --tlsVerification`
: Whether outgoing HTTPS connections should have TLS verified.
<br/>(boolean, **true**)