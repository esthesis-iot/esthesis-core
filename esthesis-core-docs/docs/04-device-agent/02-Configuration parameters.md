# Configuration parameters
The esthesis device agent supports a plethora of rutime configuration parameters to match your
environment and runtime needs. Parameters can be defined either as environment variables or as
command line arguments. The following tables lists all available configuration parameters.

<table>
	<caption>Mandatory parameters</caption>
  <thead>
    <tr>
      <th>Environment/CLI variable</th>
      <th>Description</th>
			<th>Type</th>
    </tr>
  </thead>
  <tbody>
    <tr>
			<td><code>HARDWARE_ID, hardwareId</code></td>
			<td>The unique identifier of the device.</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>REGISTRATION_URL, registrationUrl</code></td>
			<td>The URL of esthesis server to register this device with.</td>
			<td>URL</td>
		</tr>
  </tbody>
</table>

<table>
	<caption>Optional parameters</caption>
  <thead>
    <tr>
      <th>Environment/CLI variable</th>
      <th>Description</th>
			<th>Type</th>
    </tr>
  </thead>
  <tbody>
		<tr>
			<td><code>ATTRIBUTES, attributes</code></td>
			<td>A comma-separated list of key-value pairs to be sent as attributes.</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>AUTO_UPDATE, autoUpdate</code></td>
			<td>A flag indicating whether the device should try to automatically obtain newer
					firmware once per day.<br/><br/>
					Default: false</td>
			<td>Boolean</td>
		</tr>
		<tr>
			<td><code>DEMO_CATEGORY, demoCategory</code></td>
			<td>The category of data posted as demo data.<br/><br/>
					Default: demo</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>DEMO_INTERVAL, demoInterval</code></td>
			<td>The frequency in which demo data is generated (in seconds).<br/><br/>
					Default: 0</td>
			<td>Numeric</td>
		</tr>
		<tr>
			<td><code>ENDPOINT_HTTP, endpointHttp</code></td>
			<td>Whether the embedded HTTP server is enabled or not.<br/><br/>
					Default: false</td>
			<td>Boolean</td>
		</tr>
    <tr>
			<td><code>ENDPOINT_HTTP_LISTENING_IP, endpointHttpListeningIP</code></td>
			<td>The IP address where the embedded HTTP server listens to.<br/><br/>
					Default: 127.0.0.1</td>
			<td>IP address</td>
		</tr>
		<tr>
			<td><code>ENDPOINT_HTTP_LISTENING_PORT, endpointHttpListeningPort</code></td>
			<td>The port in which the embedded HTTP server listens to.<br/><br/>
					Default: 8080</td>
			<td>Numeric</td>
		</tr>
		<tr>
			<td><code>ENDPOINT_MQTT, endpointMqtt</code></td>
			<td>Whether the embedded MQTT server is enabled or not.<br/><br/>
					Default: false</td>
			<td>Boolean</td>
		</tr>
		<tr>
			<td><code>ENDPOINT_MQTT_LISTENING_IP, endpointMqttListeningIP</code></td>
			<td>The IP address where the embedded MQTT server listens to.<br/><br/>
					Default: 127.0.0.1</td>
			<td>IP address</td>
		</tr>
		<tr>
			<td><code>ENDPOINT_MQTT_LISTENING_PORT, endpointMqttListeningPort</code></td>
			<td>The port in which the embedded MQTT server listens to.<br/><br/>
					Default: 1883</td>
			<td>Numeric</td>
		</tr>
		<tr>
			<td><code>HEALTH_REPORT_INTERVAL, healthReportInterval</code></td>
			<td>The frequency to send health reports (in seconds).<br/><br/>
					Default: 300</td>
			<td>Numeric</td>
		</tr>
		<tr>
			<td><code>HTTP_RETRY, httpRetry</code></td>
			<td>The number of seconds to wait before retrying a failed HTTP request.<br/><br/>
					Default: 60</td>
			<td>Numeric</td>
		</tr>
		<tr>
			<td><code>HTTP_TIMEOUT, httpTimeout</code></td>
			<td>The number of seconds after which an HTTP call times out.<br/><br/>
					Default: 60
			</td>
			<td>Numeric</td>
		</tr>
		<tr>
			<td><code>LOG_ABBREVIATION, logAbbreviation</code></td>
			<td>The characters length to abbreviate log messages to.<br/><br/>
					Default: 1024</td>
			<td>Numeric</td>
		</tr>
		<tr>
			<td><code>LOG_LEVEL, logLevel</code></td>
			<td>The logging level to use [trace, debug, info].<br/><br/>
					Default: info</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>LUA_HTTP_TELEMETRY_SCRIPT, luaHttpTelemetryScript</code></td>
			<td>The filesystem location of a Lua script to transform incoming payloads for telemetry data pushed via the HTTP endpoint</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>LUA_HTTP_METADATA_SCRIPT, luaHttpMetadataScript</code></td>
			<td>The filesystem location of a Lua script to transform incoming payloads for metadata data pushed via the HTTP endpoint</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>LUA_MQTT_TELEMETRY_SCRIPT, luaMqttTelemetryScript</code></td>
			<td>The filesystem location of a Lua script to transform incoming payloads for telemetry data pushed via the MQTT endpoint</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>LUA_MQTT_METADATA_SCRIPT, luaMqttMetadataScript</code></td>
			<td>The filesystem location of a Lua script to transform incoming payloads for metadata data pushed via the MQTT endpoint</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>-, luaExtraMqttTelemetryTopic</code></td>
			<td>A custom MQTT telemetry endpoint to be handled by a user-defined Lua script. The first argument of this
			parameter is the name of the topic, and the second argument is the Lua script to be executed. This
			parameter can be repeated to define additional topics.</td>
			<td>Alphanumeric Alphanumeric </td>
		</tr>
		<tr>
			<td><code>-, luaExtraMqttMetadataTopic</code></td>
			<td>A custom MQTT metadata endpoint to be handled by a user-defined Lua script. The first argument of this
			parameter is the name of the topic, and the second argument is the Lua script to be executed. This
			parameter can be repeated to define additional topics.</td>
			<td>Alphanumeric Alphanumeric </td>
		</tr>
		<tr>
			<td><code>-, luaExtraHttpTelemetryEndpoint</code></td>
			<td>A custom HTTP telemetry endpoint to be handled by a user-defined Lua script. The first argument of this
			parameter is the name of the endpoint, and the second argument is the Lua script to be executed. This
			parameter can be repeated to define additional topics.</td>
			<td>Alphanumeric Alphanumeric </td>
		</tr>
		<tr>
			<td><code>-, luaExtraHttpMetadataEndpoint</code></td>
			<td>A custom HTTP metadata endpoint to be handled by a user-defined Lua script. The first argument of this
			parameter is the name of the endpoint, and the second argument is the Lua script to be executed. This
			parameter can be repeated to define additional topics.</td>
			<td>Alphanumeric Alphanumeric </td>
		</tr>
		<tr>
			<td><code>MQTT_INFLIGHT_TTL_DURATION, mqttInflightTTLDuration</code></td>
			<td>The number of seconds that a queued inflight message should exist before being purged.<br/><br/>
					Default: 60</td>
			<td>Numeric</td>
		</tr>
		<tr>
			<td><code>MQTT_TIMEOUT, mqttTimeout</code></td>
			<td>The number of seconds to wait before failing an outgoing MQTT message.<br/><br/>
					Default: 60</td>
			<td>Numeric</td>
		</tr>
    <tr>
			<td><code>PAUSE_STARTUP, pauseStartup</code></td>
			<td>A flag indicating whether the device should start paused.<br/><br/>
					Default: false</td>
			<td>Boolean</td>
		</tr>
		<tr>
			<td><code>PING_INTERVAL, pingInterval</code></td>
			<td>The frequency to ping esthesis Core (in seconds).<br/><br/>
					Default: 60</td>
			<td>Numeric</td>
		</tr>
    <tr>
			<td><code>PROPERTIES_FILE, propertiesFile</code></td>
			<td>The file to store the agent’s configuration.<br/><br/>
					Default: $HOME/.esthesis/device/esthesis.properties</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>PROVISIONING_SCRIPT, provisioningScript</code></td>
			<td>The script used to install new provisioning packages.<br/><br/>
					Default: $HOME/.esthesis/device/firmware.sh</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>REBOOT_SCRIPT, rebootScript</code></td>
			<td>The script used to reboot the device.<br/><br/>
					Default: $HOME/.esthesis/device/reboot.sh</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>REGISTRATION_SECRET, registrationSecret</code></td>
			<td>If set, the registration request will include it as a header.</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>SECURE_PROPERTIES_FILE, securePropertiesFile</code></td>
			<td>The secure file to store sensitive parts of the agent's configuration.<br/><br/>
					Default: $HOME/.esthesis/device/secure/esthesis.properties</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>SECURE_PROVISIONING, secureProvisioning</code></td>
			<td>A flag indicating whether provisioning requests should be accompanied by a signature token.<br/><br/>
					Default: false</td>
			<td>Boolean</td>
		</tr>
		<tr>
			<td><code>SHUTDOWN_SCRIPT, shutdownScript</code></td>
			<td>The script used to shut down the device.<br/><br/>
					Default: $HOME/.esthesis/device/shutdown.sh</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>SIGNATURE_ALGORITHM, signatureAlgorithm</code></td>
			<td>The algorithm to use to produce signatures.<br/><br/>
					Default: SHA256WITHRSA</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>SUPPORTED_COMMANDS, supportedCommands</code></td>
			<td>The remote commands this device supports:<br/>
				e: Execute arbitrary<br/>
				f: Firmware update<br/>
				r: Reboot<br/>
				s: Shutdown<br/>
				p: Ping<br/>
				h: Health report<br/><br/>
				Default: efrsph
			</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>TAGS, tags</code></td>
			<td>A comma-separated list of tags to associate with this device.</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>TEMP_DIR, tempDir</code></td>
			<td>The folder to temporarily store provisioning packages.<br/><br/>
					Default: OS default temp directory</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>TOPIC_COMMAND_REQUEST, topicCommandRequest</code></td>
			<td>The MQTT topic to use for command request messages.<br/><br/>
					Default: esthesis/command/request</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>TOPIC_COMMAND_REPLY, topicCommandReply</code></td>
			<td>The MQTT topic to use for command reply messages.<br/><br/>
					Default: esthesis/command/reply</td>
			<td>Alphanumeric</td>
		</tr>
	  <tr>
			<td><code>TOPIC_DEMO, topicDemo</code></td>
			<td>The MQTT topic to post demo data.<br/><br/>
					Default: esthesis/telemetry</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>TOPIC_METADATA, topicMetadata</code></td>
			<td>The MQTT topic to use for metadata messages.<br/><br/>
					Default: esthesis/metadata</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>TOPIC_PING, topicPing</code></td>
			<td>The MQTT topic to use for ping messages.<br/><br/>
					Default: esthesis/ping</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>TOPIC_TELEMETRY, topicTelemetry</code></td>
			<td>The MQTT topic to use for telemetry messages.<br/><br/>
					Default: esthesis/telemetry</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>VERSION_FILE, versionFile</code></td>
			<td>A file with a single line of text depicting the current version of the firmware running on the device.<br/><br/>
					Default: $HOME/.esthesis/device/version</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>VERSION_REPORT_TOPIC, versionReportTopic</code></td>
			<td>The MQTT topic to report the firmware version.<br/><br/>
					Default: esthesis/metadata</td>
			<td>Alphanumeric</td>
		</tr>
		<tr>
			<td><code>VERSION_REPORT, versionReport</code></td>
			<td>Report the version number available in the specified version file once during boot.<br/><br/>
					Default: false</td>
			<td>Boolean</td>
		</tr>
  </tbody>
</table>
