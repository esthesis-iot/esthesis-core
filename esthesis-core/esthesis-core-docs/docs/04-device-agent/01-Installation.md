## Installation

Depending on your environment, you may want to run the esthesis device agent as a Docker container,
or as a standalone executable. The following sections describe how to install the device agent in
both scenarios.

:::tip
To have your device communicating with esthesis Core, you must have registered an MQTT server
under Integrations > Infrastructure. In order for esthesis Core to be able to process data coming
from your device, you must have also defined the appropriate dataflows under Integrations > Dataflows.
:::

### Docker
The esthesis device agent can be found in Docker hub as the
[esthesis core device](https://hub.docker.com/repository/docker/esthesisiot/esthesis-core-device/general)
image. The esthesis device agent supports a plethora of configuration options (see
[Configuration parameters](02-Configuration%20parameters.md)), however the minimum configuration
required to successfully start it up and have it connect to esthesis Core is the following:

```shell
docker run \
	-e HARDWARE_ID=test-device-1 \
	-e REGISTRATION_URL=http://esthesis-core.domain/api/agent/v1/register \
	esthesisiot/esthesis-core-device
```

:::caution
1. You must replace the `REGISTRATION_URL` environment variable with the hostname matching your installation.
2. You must create/mount a data volume to the container, so that the device agent can persist
its state. If you do not do so, the device agent will not be able to reconnect to esthesis Core
after a restart, instead it will create to re-register (and probably fail if you have kept the
`HARDWARE_ID` unchanged).
:::

### Standalone executable
Download the standalone executable matching your operating system and architecture from the release
page. Start the device agent with the following command:

```shell
./esthesis-agent_darwin-arm64 \
	--hardwareId=test-device-1 \
	--registrationUrl=http://192.168.22.10/api/agent/v1/register
```

:::tip
1. Choose the correct esthesis device agent that matches your operating system and architecture.
2. Using the above configuration, the esthesis device agent will persist data under
`$HOME/.esthesis/device`.
:::
