# Developing esthesis Device Agent

The esthesis Device Agent is a software component that runs on the device and connects it to the
esthesis CORE. The agent is created in Go and can be compiled for any platform supported by Go.

## Run and compile

:::tip
When running the agent for the first time for a specific hardware Id, make sure that the tag you
specify via `--tags` exists in esthesis CORE, and it is assigned to the registered MQTT server in
infrastructure page.
:::

### Compile locally

To compile the agent locally go inside `go` directory and execute:

```shell
go build -o esthesis-agent cmd/main.go
```

The above will create an `esthesis-agent` executable for your platform. This is useful to check
that the agent compiles and runs on your machine, but not very useful for development.

### Run locally

To run the agent locally switch to `go` directory and execute:

```shell
HID=abc001 && \
REGISTRATION_URL=http://localhost:59070/api/v1/register && \
go run cmd/main.go \
    --hardwareId=$HID \
    --registrationUrl=$REGISTRATION_URL \
    --propertiesFile=$HOME/.esthesis/device/$HID/esthesis.properties \
    --securePropertiesFile=$HOME/.esthesis/device/$HID/secure/esthesis.properties \
    --tempDir=$HOME/.esthesis/device/$HID/temp \
    --versionFile=$HOME/.esthesis/device/$HID/version \
    --provisioningScript=$HOME/.esthesis/device/$HID/firmware.sh \
    --autoUpdate=false --secureProvisioning=true \
    --versionReport=true \
    --tags=dev \
    --logLevel=debug
```

### Run locally, automatically recompiling on changes

If you want your agent to automatically recompile and restart on changes, you can use
[air](https://github.com/cosmtrek/air). To run the agent locally switch to `go` directory and
execute:

```shell
HID=abc125 && \
REGISTRATION_URL=http://localhost:59070/api/v1/register && \
air --build.cmd "go build -o /tmp/esthesis-core-device cmd/main.go" --build.bin "/tmp/esthesis-core-device" -- \
	--hardwareId=$HID \
	--registrationUrl=$REGISTRATION_URL \
	--propertiesFile=$HOME/.esthesis/device/$HID/esthesis.properties \
	--securePropertiesFile=$HOME/.esthesis/device/$HID/secure/esthesis.properties \
	--tempDir=$HOME/.esthesis/device/$HID/temp \
	--versionFile=$HOME/.esthesis/device/$HID/version \
	--provisioningScript=$HOME/.esthesis/device/$HID/firmware.sh \
	--autoUpdate=true --secureProvisioning=true \
	--versionReport=true \
	--tags=dev \
	--logLevel=debug
```

## Testing multiple agents

### Using containers
To execute one (or more) device agents in Docker containers running on your local machine, connecting
to your development esthesis CORE instance, you need to prepare the following:

1. A container running `kubefwd` to forward the Mosquitto service:
	```shell
	docker run -d --privileged \
 		--add-host=host.docker.internal:host-gateway \
 		--name kubefwd \
		-v "$(echo $HOME)/.kube/config":/root/.kube/config \
		txn2/kubefwd services -d esthesis -n esthesis -f metadata.name=mosquitto
	```
2. A container running the device agent, obtaining its network via the container you created above:
	```shell
	REGISTRY_URL=<your-registry> && \
	RND_PREFIX=esthesis-test-device-$(uuidgen | cut -f1 -d"-" | awk '{print tolower($0)}') && \
	for ((i=1; i<=3; i++)); do
		HID=$RND_PREFIX-$i && \
		docker run -d --name $HID \
			-e HARDWARE_ID=$HID \
			-e REGISTRATION_URL=http://host.docker.internal:59070/api/v1/register \
			-e PROPERTIES_FILE=/app/.esthesis/esthesis.properties \
			-e SECURE_PROPERTIES_FILE=/app/.esthesis/secure/esthesis.properties \
			-e TEMP_DIR=/app/.esthesis/temp \
			-e VERSION_FILE=/app/version \
			-e PROVISIONING_SCRIPT=/app/firmware-update.sh \
			-e LOG_LEVEL=debug \
			-e AUTO_UPDATE=false \
			-e SECURE_PROVISIONING=true \
 			-e TAGS=dev \
			--net container:kubefwd \
			$REGISTRY_URL/esthesisiot/esthesis-core-device:latest
	done
	```

### Using Kubernetes

```shell
REGISTRY_URL=<your-registry> && \
RND_PREFIX=esthesis-test-device-$(uuidgen | cut -f1 -d"-" | awk '{print tolower($0)}') && \
for ((i=1; i<=3; i++)); do
	HID=$RND_PREFIX-$i && \
	kubectl run $HID \
		--image $REGISTRY_URL/esthesisiot/esthesis-core-device:latest \
		--image-pull-policy=Always \
		--env="HARDWARE_ID=$HID" \
		--env="REGISTRATION_URL=http://<your-host-machine>:59070/api/v1/register" \
		--env="PROPERTIES_FILE=/app/.esthesis/esthesis.properties" \
		--env="SECURE_PROPERTIES_FILE=/app/.esthesis/secure/esthesis.properties" \
		--env="TEMP_DIR=/app/.esthesis/temp" \
		--env="VERSION_FILE=/app/.esthesis/version" \
		--env="PROVISIONING_SCRIPT=/app/.esthesis/firmware.sh" \
		--env="LOG_LEVEL=debug" \
		--env="AUTO_UPDATE=false" \
		--env="SECURE_PROVISIONING=true" \
		--env="TAGS=dev" \
done
```
