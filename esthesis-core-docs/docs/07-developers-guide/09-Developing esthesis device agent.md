# Developing esthesis Device Agent

The esthesis Device Agent is a software component that runs on the device and connects it to the
esthesis Core. The agent is created in Go and can be compiled for any platform supported by Go.

## Run and compile

:::tip
When running the agent for the first time for a specific hardware Id, make sure that the tag you
specify via `--tags` exists in esthesis CORE and it is assigned to the registered MQTT server in
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

To run the agent locally go inside `go` directory and execute:

```shell
HID=abc123 && \
REGISTRATION_URL=http://apisix-gateway.esthesis/api/agent/v1/register && \
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
    --logLevel=debug
```

### Run locally, automatically recompiling on changes

If you want your agent to automatically recompile and restart on changes, you can use
[air](https://github.com/cosmtrek/air). To run the agent locally go inside `go` directory and
execute:

```shell
HID=abc125 && \
REGISTRATION_URL=http://apisix-gateway.esthesis/api/agent/v1/register && \
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
	--logLevel=debug
```

## Testing multiple agents

### Using containers

```shell
APISIX_IP=$(dig +short apisix-gateway.esthesis) && \
RND_PREFIX=esthesis-test-device-$(uuidgen | cut -f1 -d"-" | awk '{print tolower($0)}') && \
for ((i=1; i<=3; i++)); do
	HID=$RND_PREFIX-$i && \
	docker run -d --name $HID \
		-e HARDWARE_ID=$HID \
		-e REGISTRATION_URL=http://apisix-gateway.esthesis/api/agent/v1/register \
		-e PROPERTIES_FILE=/app/.esthesis/esthesis.properties \
		-e SECURE_PROPERTIES_FILE=/app/.esthesis/secure/esthesis.properties \
		-e TEMP_DIR=/app/.esthesis/temp \
		-e VERSION_FILE=/app/version \
		-e PROVISIONING_SCRIPT=/app/firmware-update.sh \
		-e LOG_LEVEL=debug \
		-e AUTO_UPDATE=false \
		-e SECURE_PROVISIONING=true \
		--add-host apisix-gateway.esthesis:$APISIX_IP \
		$REGISTRY_URL/esthesisiot/esthesis-core-device:latest-debug
done
```

### Using Kubernetes

```shell
RND_PREFIX=esthesis-test-device-$(uuidgen | cut -f1 -d"-" | awk '{print tolower($0)}') && \
for ((i=1; i<=3; i++)); do
	HID=$RND_PREFIX-$i && \
	kubectl run $HID \
		--image $REGISTRY_URL/esthesisiot/esthesis-core-device:latest-debug \
		--image-pull-policy=Always \
		--env="HARDWARE_ID=$HID" \
		--env="REGISTRATION_URL=http://apisix-gateway/api/agent/v1/register" \
		--env="PROPERTIES_FILE=/app/.esthesis/esthesis.properties" \
		--env="SECURE_PROPERTIES_FILE=/app/.esthesis/secure/esthesis.properties" \
		--env="TEMP_DIR=/app/.esthesis/temp" \
		--env="VERSION_FILE=/app/.esthesis/version" \
		--env="PROVISIONING_SCRIPT=/app/.esthesis/firmware.sh" \
		--env="LOG_LEVEL=debug" \
		--env="AUTO_UPDATE=false" \
		--env="TAGS=k8s" \
		--env="SECURE_PROVISIONING=true"
done
```
