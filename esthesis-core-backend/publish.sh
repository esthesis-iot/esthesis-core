#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-backend modules to a container registry.
#
# Environment variables:
#   ESTHESIS_REGISTRY_USERNAME: The username to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_PASSWORD: The password to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_URL: The URL of the registry to push to (default: docker.io).
#   ESTHESIS_ARCHITECTURES: The architectures to build (default: linux/amd64).
#   ESTHESIS_BUILDX_KUBERNETES: If set to true, a builder will be created in Kubernetes (default: false).
#
# Usage:
#   ./publish.sh
#   ./publish.sh <module-path> <module-name>
#
# Example:
#   ./publish.sh services/srv-about/srv-about-impl srv-about
####################################################################################################

# Trap exit.
set -e
exit_handler() {
    printError "Build failed with exit code $?"
    if [ -n "$BUILDX_NAME" ]; then
				printInfo "Deleting Docker buildx $BUILDX_NAME."
				docker buildx rm "$BUILDX_NAME"
		fi
    exit 1
}
trap exit_handler ERR

# Helper functions to print messages.
printError() {
	printf "\e[31m***ERROR: $1\e[0m\n"
}
printInfo() {
	printf "\e[32m***INFO: $1\e[0m\n"
}

# Check if Podman is installed.
if [ -x "$(command -v podman)" ]; then
    # Check if TESTCONTAINERS_RYUK_DISABLED is set
    if [ -z "$TESTCONTAINERS_RYUK_DISABLED" ]; then
        printInfo "Setting TESTCONTAINERS_RYUK_DISABLED=true due to Podman being detected."
        export TESTCONTAINERS_RYUK_DISABLED=true
    fi
fi

# If $ESTHESIS_REGISTRY_URL is empty, set it to docker.io.
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
  ESTHESIS_REGISTRY_URL="docker.io"
fi

# Set buildx driver options.
if [ -z "$ESTHESIS_BUILDX_KUBERNETES" ]; then
  ESTHESIS_BUILDX_KUBERNETES="false"
fi

# Find the version of the package.
PACKAGE_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.version | fgrep -v "[INFO]")
printInfo "Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io" ]]; then
    printError "Cannot push a snapshot version to docker.io."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64"
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64"
fi

# Array with all modules to be published.
modules=(
    "dataflows/dfl-command-reply-updater" "dfl-command-reply-updater"
    "dataflows/dfl-influxdb-writer" "dfl-influxdb-writer"
    "dataflows/dfl-mqtt-client" "dfl-mqtt-client"
    "dataflows/dfl-orion-gateway" "dfl-orion-gateway"
    "dataflows/dfl-ping-updater" "dfl-ping-updater"
    "dataflows/dfl-rdbms-writer" "dfl-rdbms-writer"
    "dataflows/dfl-redis-cache" "dfl-redis-cache"
    "services/srv-about/srv-about-impl" "srv-about"
    "services/srv-agent/srv-agent-impl" "srv-agent"
    "services/srv-application/srv-application-impl" "srv-application"
    "services/srv-audit/srv-audit-impl" "srv-audit"
    "services/srv-campaign/srv-campaign-impl" "srv-campaign"
    "services/srv-command/srv-command-impl" "srv-command"
    "services/srv-crypto/srv-crypto-impl" "srv-crypto"
    "services/srv-dataflow/srv-dataflow-impl" "srv-dataflow"
    "services/srv-device/srv-device-impl" "srv-device"
    "services/srv-dt/srv-dt-impl" "srv-dt"
    "services/srv-infrastructure/srv-infrastructure-impl" "srv-infrastructure"
    "services/srv-kubernetes/srv-kubernetes-impl" "srv-kubernetes"
    "services/srv-provisioning/srv-provisioning-impl" "srv-provisioning"
    "services/srv-public-access/srv-public-access-impl" "srv-public-access"
    "services/srv-security/srv-security-impl" "srv-security"
    "services/srv-settings/srv-settings-impl" "srv-settings"
    "services/srv-tag/srv-tag-impl" "srv-tag"
)

# If the user has provided a specific module to be published, only publish that one.
if [ $# -eq 2 ]; then
	modules=(
		"$1" "$2"
	)
fi

# Create a Docker buildx.
BUILDX_NAME=$(LC_CTYPE=C tr -dc 'a-zA-Z' < /dev/urandom | head -c 1)$(LC_CTYPE=C tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 11)
printInfo "Creating Docker buildx $BUILDX_NAME."
if [ "$ESTHESIS_BUILDX_KUBERNETES" = "true" ]; then
	docker buildx create --driver kubernetes --name "$BUILDX_NAME" --use --config buildkitd.toml
else
	docker buildx create --name "$BUILDX_NAME" --use --config buildkitd.toml
fi

# Login to remote registry.
if [ -n "$ESTHESIS_REGISTRY_USERNAME" ] && [ -n "$ESTHESIS_REGISTRY_PASSWORD" ]; then
	docker login "$ESTHESIS_REGISTRY_URL" --username "$ESTHESIS_REGISTRY_USERNAME" --password "$ESTHESIS_REGISTRY_PASSWORD"
fi

# Iterate over all modules and publish them.
for ((i = 0; i < ${#modules[@]}; i += 2)); do
	MODULE_PATH="${modules[$i]}"
	MODULE_NAME="${modules[$i+1]}"
	IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesisiot/esthesis-core-$MODULE_NAME"
	printInfo "Building $ESTHESIS_ARCHITECTURES for $IMAGE_NAME."
	pushd .

	cd "$MODULE_PATH" || exit
	./mvnw clean package

	TAGS=("latest" "$PACKAGE_VERSION")
	for TAG in "${TAGS[@]}"; do
		printInfo "Building container $IMAGE_NAME:$TAG."
		docker buildx build \
					 -f src/main/docker/Dockerfile.jvm \
					 --platform "$ESTHESIS_ARCHITECTURES" \
					 -t "$IMAGE_NAME:$TAG" \
					 --push .
	done

	popd || exit
done

# Delete the buildx.
printInfo "Deleting Docker buildx $BUILDX_NAME."
docker buildx rm "$BUILDX_NAME"
