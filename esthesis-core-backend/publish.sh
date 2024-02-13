#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-backend modules to a container registry.
#
# Environment variables:
#   ESTHESIS_REGISTRY_USERNAME: The username to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_PASSWORD: The password to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_URL: 			The URL of the registry to push to (default: docker.io/esthesisiot).
#   ESTHESIS_ARCHITECTURES: 		The architectures to build (default: linux/amd64).
#   ESTHESIS_BUILDX_KUBERNETES: If set to true, a builder will be created in Kubernetes
#   														(default: false).
#		ESTHESIS_GLOBAL_BUILD: 			If set to true, a global build is performed first. This is to build
#																dependencies that are shared between modules (default: false).
#
# Usage:
#   ./publish.sh
#   ./publish.sh <module-path> <module-name>
#   ./publish.sh dfl
#   ./publish.sh srv
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
  ESTHESIS_REGISTRY_URL="docker.io/esthesisiot"
fi

# If $ESTHESIS_GLOBAL_BUILD is empty, set it to false.
if [ -z "$ESTHESIS_GLOBAL_BUILD" ]; then
  ESTHESIS_GLOBAL_BUILD="false"
fi

# Set buildx driver options.
if [ -z "$ESTHESIS_BUILDX_KUBERNETES" ]; then
  ESTHESIS_BUILDX_KUBERNETES="false"
fi

# Find the version of the package.
PACKAGE_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.version | fgrep -v "[INFO]")
printInfo "Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io/esthesisiot" ]]; then
    printError "Cannot push a snapshot version to docker.io/esthesisiot."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64"
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64"
fi

# Arrays with all modules to be published.
modulesDataFlows=(
	"dataflows/dfl-command-reply-updater" "dfl-command-reply-updater"
	"dataflows/dfl-influxdb-writer" "dfl-influxdb-writer"
	"dataflows/dfl-mqtt-client" "dfl-mqtt-client"
	"dataflows/dfl-orion-gateway" "dfl-orion-gateway"
	"dataflows/dfl-ping-updater" "dfl-ping-updater"
	"dataflows/dfl-rdbms-writer" "dfl-rdbms-writer"
	"dataflows/dfl-redis-cache" "dfl-redis-cache"
)
modulesServices=(
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

# Check what should be published.
if [ $# -eq 2 ]; then
	modules=(
		"$1" "$2"
	)
elif [ $# -eq 1 ] && [ $1 = "dfl" ]; then
	modules=(
		"${modulesDataFlows[@]}"
	)
elif [ $# -eq 1 ] && [ $1 = "srv" ]; then
	modules=(
		"${modulesServices[@]}"
	)
else
	modules=(
		"${modulesServices[@]}"
		"${modulesDataFlows[@]}"
	)
fi

# Before start building the requested module(s), check if a global build needs to be performed
# first. This is to build dependencies that are shared between modules.
if [ "$ESTHESIS_GLOBAL_BUILD" = "true" ]; then
	printInfo "Performing global build."
	./mvnw clean package
fi

# Create a Docker buildx.
BUILDX_NAME=$(LC_CTYPE=C tr -dc 'a-zA-Z' < /dev/urandom | head -c 1)$(LC_CTYPE=C tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 11)
printInfo "Creating Docker buildx $BUILDX_NAME."
if test -f "buildkitd.toml"; then
    BUILDKIT_CONFIG="--config buildkitd.toml"
else
		BUILDKIT_CONFIG=""
fi
if [ "$ESTHESIS_BUILDX_KUBERNETES" = "true" ]; then
  docker buildx create --driver kubernetes --name "$BUILDX_NAME" --use $BUILDKIT_CONFIG
else
  docker buildx create --name "$BUILDX_NAME" --use $BUILDKIT_CONFIG
fi

# Login to remote registry.
if [ -n "$ESTHESIS_REGISTRY_USERNAME" ] && [ -n "$ESTHESIS_REGISTRY_PASSWORD" ]; then
	docker login "$ESTHESIS_REGISTRY_URL" --username "$ESTHESIS_REGISTRY_USERNAME" --password "$ESTHESIS_REGISTRY_PASSWORD"
fi

# Iterate over all modules and publish them.
for ((i = 0; i < ${#modules[@]}; i += 2)); do
	MODULE_PATH="${modules[$i]}"
	MODULE_NAME="${modules[$i+1]}"
	IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesis-core-$MODULE_NAME"
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
