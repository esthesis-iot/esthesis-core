# Installation

## Installation

Depending on your environment, you may want to run the esthesis device agent as a Docker container,
or as a standalone executable. The following sections describe how to install the device agent in
both scenarios.

<tip>
To have your device communicating with esthesis CORE, you must have registered an MQTT server
under Integrations > Infrastructure. In order for esthesis CORE to be able to process data coming
from your device, you must have also defined the appropriate dataflows under Integrations > Dataflows.
</tip>

### Docker

The esthesis device agent can be found in Docker hub as the
[](https://hub.docker.com/repository/docker/esthesisiot/esthesis-core-device/general)
image. The esthesis device agent supports a plethora of configuration options (see
[](configuration-parameters.md)), however the minimum configuration
required to successfully start it up and have it connect to esthesis CORE is the following:

```shell
docker run \
	-e HARDWARE_ID=test-device-1 \
	-e REGISTRATION_URL=http://esthesis-core.domain/api/agent/v1/register \
	esthesisiot/esthesis-core-device
```

<note>

1. You must replace the `REGISTRATION_URL` environment variable with the hostname matching your installation.

2. You must create/mount a data volume to the container, so that the device agent can persist
its state. If you do not do so, the device agent will not be able to reconnect to esthesis CORE
after a restart, instead it will create to re-register (and probably fail if you have kept the
`HARDWARE_ID` unchanged).
</note>

Docker images are maintained for the following architectures:
- linux/arm/v6
- linux/arm/v7
- linux/arm64
- linux/amd64

### Standalone executable
Download the standalone executable matching your operating system and architecture from the [releases
page](https://github.com/esthesis-iot/esthesis-core/releases). Start the device agent with the following command:

```shell
./esthesis-core-device \
	--hardwareId=test-device-1 \
	--registrationUrl=http://192.168.22.10/api/agent/v1/register
```

<note>

1. Choose the correct esthesis device agent that matches your operating system and architecture.

2. Using the above configuration, the esthesis device agent will persist data under
`$HOME/.esthesis/device`.
</note>

Native standalone executables are maintained for the following architectures:
- darwin/amd64
- darwin/arm64
- linux/amd64
- linux/arm/5
- linux/arm/6
- linux/arm/7
- linux/arm64
- windows/386
- windows/amd64
