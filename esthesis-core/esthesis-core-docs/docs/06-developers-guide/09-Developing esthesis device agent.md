# Developing esthesis Device Agent
The esthesis Device Agent is a software component that runs on the device and connects it to the
esthesis Core. The agent is created in Go and can be compiled for any platform supported by Go.

## Run and compile

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
REGISTRATION_URL=http://apisix-gateway.esthesis.localdev/api/agent/v1/register && \
go run cmd/main.go \
    --hardwareId=$HID \
    --registrationUrl=$REGISTRATION_URL \
    --tags=tag1 \
    --propertiesFile=$HOME/.esthesis/device/$HID/esthesis.properties \
    --securePropertiesFile=$HOME/.esthesis/device/$HID/secure/esthesis.properties \
    --tempDir=$HOME/.esthesis/device/$HID/temp \
    --versionFile=$HOME/.esthesis/device/$HID/version \
    --provisioningScript=$HOME/.esthesis/device/$HID/firmware.sh \
    --autoUpdate=false --secureProvisioning=true \
    --versionReport=true \
    --logLevel=debug
```

### Run locally, automatically recompile on changes
If you want your agent to automatically recompile and restart on changes, you can use
[air](https://github.com/cosmtrek/air). To run the agent locally go inside `go` directory and
execute:
```shell
HID=abc123 && \
REGISTRATION_URL=http://apisix-gateway.esthesis.localdev/api/agent/v1/register && \
air --build.cmd "go build -o /tmp/esthesis-core-device cmd/main.go" --build.bin "/tmp/esthesis-core-device" -- \
	--hardwareId=$HID \
	--registrationUrl=$REGISTRATION_URL \
	--tags=tag1 \
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

### Using Docker
```shell
HID=$(uuidgen | cut -f1 -d"-" | awk '{print tolower($0)}') && \
docker run --rm esthesisiot/esthesis-agent:3.0.0-SNAPSHOT /app/esthesis-agent \
--hardwareId=$HID \
--registrationUrl=http://192.168.21.2/api/agent/v1/register \
--tags=tag1 \
--propertiesFile=/app/.esthesis/esthesis.properties \
--securePropertiesFile=/app/.esthesis/secure/esthesis.properties \
--tempDir=/app/.esthesis/temp \
--versionFile=/app/.esthesis/version \
--provisioningScript=/app/.esthesis/firmware.sh \
--logLevel=debug \
--autoUpdate=false --secureProvisioning=true
```

Note: You need to change the IP address of `registrationUrl` to the IP address of APISIX Gateway.

### Using Kubernetes
```shell
RND_PREFIX=test-$(uuidgen | cut -f1 -d"-" | awk '{print tolower($0)}') && \
for ((i=1; i<=3; i++)); do
HID=$RND_PREFIX-$i && \
kubectl run $HID --image esthesisiot/esthesis-agent:3.0.0-SNAPSHOT --image-pull-policy='Always' -- \
/app/esthesis-agent \
--hardwareId=$HID \
--registrationUrl=http://apisix-gateway/api/agent/v1/register \
--tags=tag1 \
--propertiesFile=/app/.esthesis/esthesis.properties \
--securePropertiesFile=/app/.esthesis/secure/esthesis.properties \
--tempDir=/app/.esthesis/temp \
--versionFile=/app/.esthesis/version \
--provisioningScript=/app/.esthesis/firmware.sh \
--logLevel=debug \
--autoUpdate=false --secureProvisioning=true
done
```
