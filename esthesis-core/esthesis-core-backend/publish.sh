#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-backend modules to a container registry.
#
# Environment variables:
#   ESTHESIS_REGISTRY_USERNAME: The username to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_PASSWORD: The password to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_URL: The URL of the registry to push to (default: docker.io).
#   ESTHESIS_ARCHITECTURES: The architectures to build (default: linux/amd64).
#
# Usage:
#   ./publish.sh
#   ./publish.sh <module-path> <module-name>
#
# Example:
#   ./publish.sh services/srv-about/srv-about-impl srv-about
####################################################################################################

# Helper functions to print messages.
printError() {
	printf "\e[31m***ERROR: "
  	for i in "$@"; do printf "%s" "$i"; done;
  	printf "\e[0m\n"
}
printInfo() {
	printf "\e[32m***INFO: "
	for i in "$@"; do printf "%s " "$i"; done;
	printf "\e[0m\n"
}

# Check if Podman is installed.
if [ -x "$(command -v podman)" ]; then
    # Check if TESTCONTAINERS_RYUK_DISABLED is set
    if [ -z "$TESTCONTAINERS_RYUK_DISABLED" ]; then
        printInfo "Setting TESTCONTAINERS_RYUK_DISABLED=true due to Podman being detected."
        export TESTCONTAINERS_RYUK_DISABLED=true
    fi

    # Check if Podman machine is running.
      if ! podman machine inspect &> /dev/null; then
        printInfo "Podman machine is not running."
        exit 6
      fi
else
		printError "Podman is not installed."
		exit 5
fi

# Registry to push to.
PUBLIC_REGISTRY=false
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
	ESTHESIS_REGISTRY_URL="docker.io"
fi
if [[ "$ESTHESIS_REGISTRY_URL" == "docker.io" ]]; then
    PUBLIC_REGISTRY=true
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
JOBS=$(echo "$ESTHESIS_ARCHITECTURES" | tr ',' '\n' | wc -l | sed -e 's/^[[:space:]]*//')

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

# Iterate over all modules and publish them.
unset CREDS
if [ -n "$ESTHESIS_REGISTRY_USERNAME" ] && [ -n "$ESTHESIS_REGISTRY_PASSWORD" ]; then
	CREDS="--creds $ESTHESIS_REGISTRY_USERNAME:$ESTHESIS_REGISTRY_PASSWORD"
fi
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
		if podman manifest exists "$IMAGE_NAME:$TAG"; then
			printInfo "Removing existing manifest $IMAGE_NAME:$TAG."
			podman manifest rm "$IMAGE_NAME:$TAG"
		fi
		printInfo "Building container $IMAGE_NAME:$TAG."
		podman build \
					 --jobs "$JOBS" \
					 -f src/main/docker/Dockerfile.jvm \
					 --platform "$ESTHESIS_ARCHITECTURES" \
					 --manifest "$IMAGE_NAME:$TAG" .
		printInfo "Pushing container $IMAGE_NAME:$TAG."
		if [ "$PUBLIC_REGISTRY" = true ]; then
			podman manifest push "$IMAGE_NAME:$TAG" $CREDS --rm
		else
			podman manifest push "$IMAGE_NAME:$TAG" $CREDS --rm --tls-verify=false
		fi
	done

	popd || exit
done
