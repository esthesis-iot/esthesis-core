#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-backend modules to a container registry.
#
# Environment variables:
#   ESTHESIS_REGISTRY_URL: 			The URL of the registry to push to
#   														(default: docker.io/esthesisiot).
#  	ESTHESIS_REGISTRY_TYPE:			aws: Login will be attempted using 'aws ecr-public get-login-password'.
#  															auth: Login will be attempted using username and password.
#  															open:	No login will be attempted.
#  															(default: auth).
#   ESTHESIS_REGISTRY_USERNAME:	The username to login to the 'auth' type registry.
#   ESTHESIS_REGISTRY_PASSWORD:	The password to login to the 'auth' type registry.
#   ESTHESIS_ARCHITECTURES: 		The architectures to build, e.g. linux/amd64,linux/arm64
#   														(default: linux/amd64,linux/arm64).
#		ESTHESIS_GLOBAL_BUILD: 			If set to true, a global build is performed first. This is to build
#																dependencies that are shared between modules (default: false).
#   ESTHESIS_LOCAL_BUILD: 			If set to false, individual modules are not built. This is helpful
#																when this script is used as part of another script (for example,
#																a release script) which already performs a build (default: true).
#		ESTHESIS_PARALLEL_BUILD:		If set to true, modules are built in parallel (default: false).
#
# Usage examples:
#   ./publish.sh
#   ./publish.sh services/srv-about/srv-about-impl srv-about
#   ./publish.sh dfl
#   ./publish.sh srv
#   ESTHESIS_REGISTRY_TYPE=open ESTHESIS_REGISTRY_URL=192.168.50.211:5000/esthesis ./publish.sh dfl
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

# If $ESTHESIS_REGISTRY_URL is empty, set it to aws.
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
  ESTHESIS_REGISTRY_URL="docker.io/esthesisiot"
fi

# If $ESTHESIS_REGISTRY_TYPE is empty, set it to aws.
if [ -z "$ESTHESIS_REGISTRY_TYPE" ]; then
  ESTHESIS_REGISTRY_TYPE="auth"
fi

# If $ESTHESIS_GLOBAL_BUILD is empty, set it to false.
if [ -z "$ESTHESIS_GLOBAL_BUILD" ]; then
  ESTHESIS_GLOBAL_BUILD="false"
fi

# If $ESTHESIS_LOCAL_BUILD is empty, set it to true.
if [ -z "$ESTHESIS_LOCAL_BUILD" ]; then
  ESTHESIS_LOCAL_BUILD="true"
fi

# if $ESTHESIS_PARALLEL_BUILD is empty, set it to false.
if [ -z "$ESTHESIS_PARALLEL_BUILD" ]; then
	ESTHESIS_PARALLEL_BUILD="false"
fi

# Set MVNW location.
MVNW="$(pwd)/mvnw"
printInfo "Using $MVNW."

# Check mandatory environment variables.
if [ "$ESTHESIS_REGISTRY_TYPE" = "auth" ]; then
	if [ -z "$ESTHESIS_REGISTRY_USERNAME" ]; then
			printError "ESTHESIS_REGISTRY_USERNAME is not set."
			exit 1
  fi
  if [ -z "$ESTHESIS_REGISTRY_PASSWORD" ]; then
			printError "ESTHESIS_REGISTRY_PASSWORD is not set."
			exit 1
	fi
fi

MAVEN_OPTIMISE_PARAMS="-DskipTests -Dmaven.test.skip=true -T 1C"

# Find the version of the package.
PACKAGE_VERSION=$(grep -A 1 '<artifactId>esthesis-core</artifactId>' pom.xml | grep '<version>' | sed -E 's/.*<version>(.*)<\/version>.*/\1/')
if [ -z "$PACKAGE_VERSION" ]; then
	printError "Could not set PACKAGE_VERSION."
	exit 1
fi
printInfo "Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io/esthesisiot" ]]; then
    printError "Cannot push a snapshot version to docker.io/esthesisiot."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64"
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64,linux/arm64"
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
	$MVNW clean package $MAVEN_OPTIMISE_PARAMS
fi

# Create a Docker buildx.
BUILDX_NAME=$(LC_CTYPE=C tr -dc 'a-zA-Z' < /dev/urandom | head -c 1)$(LC_CTYPE=C tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 11)
printInfo "Creating Docker buildx $BUILDX_NAME."
if test -f "buildkitd.toml"; then
    BUILDKIT_CONFIG="--config buildkitd.toml"
else
		BUILDKIT_CONFIG=""
fi
docker buildx create --name "$BUILDX_NAME" --use $BUILDKIT_CONFIG

# Login to remote registry.
if [ "$ESTHESIS_REGISTRY_TYPE" = "aws" ]; then
	aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin $ESTHESIS_REGISTRY_URL
elif [ "$ESTHESIS_REGISTRY_TYPE" = "auth" ]; then
	docker login "$ESTHESIS_REGISTRY_URL" --username "$ESTHESIS_REGISTRY_USERNAME" --password "$ESTHESIS_REGISTRY_PASSWORD"
fi

# Iterate over all modules and publish them.
declare -a pids
for ((i = 0; i < ${#modules[@]}; i += 2)); do
	MODULE_PATH="${modules[$i]}"
	MODULE_NAME="${modules[$i+1]}"
	IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesis-core-$MODULE_NAME"
	printInfo "Building $ESTHESIS_ARCHITECTURES for $IMAGE_NAME."

	pushd .
	cd "$MODULE_PATH" || exit
	printInfo "Switching to $MODULE_NAME on $(pwd)."
	if [ "$ESTHESIS_LOCAL_BUILD" = "true" ]; then
		printInfo "Building module $MODULE_NAME."
		$MVNW clean package "$MAVEN_OPTIMISE_PARAMS"
	"fi"

	printInfo "Building container $IMAGE_NAME:$PACKAGE_VERSION"
	if [ "$ESTHESIS_PARALLEL_BUILD" = "true" ]; then
		docker buildx build \
			-f src/main/docker/Dockerfile.jvm \
			--platform "$ESTHESIS_ARCHITECTURES" \
			-t "$IMAGE_NAME:$PACKAGE_VERSION" \
			-t "$IMAGE_NAME:latest" \
			--push . &
		pids+=($!)
	else
		docker buildx build \
			-f src/main/docker/Dockerfile.jvm \
			--platform "$ESTHESIS_ARCHITECTURES" \
			-t "$IMAGE_NAME:$PACKAGE_VERSION" \
			-t "$IMAGE_NAME:latest" \
			--push .
	fi
	popd || exit
done

# Wait for all processes to finish
for pid in "${pids[@]}"; do
    wait "$pid"
done

# Delete the buildx.
printInfo "Deleting Docker buildx $BUILDX_NAME."
docker buildx rm "$BUILDX_NAME"
