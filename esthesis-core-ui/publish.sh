#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-ui modules to a container registry.
#
# Required environment variables:
#   ESTHESIS_REGISTRY_USERNAME: The username to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_PASSWORD: The password to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_URL: The URL of the registry to push to (default: docker.io/esthesisiot).
#   ESTHESIS_ARCHITECTURES: The architectures to build (default: linux/amd64).
#   ESTHESIS_BUILDX_KUBERNETES: If set to true, a builder will be created in Kubernetes (default: false).
#
# Usage:
#   ./publish.sh
####################################################################################################

# Trap exit.
set -e
exit_handler() {
    echo "*** ERROR: Build failed with exit code $?"
    if [ -n "$BUILDX_NAME" ]; then
				echo "*** INFO: Deleting Docker buildx $BUILDX_NAME."
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

# If $ESTHESIS_REGISTRY_URL is empty, set it to docker.io.
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
  ESTHESIS_REGISTRY_URL="docker.io/esthesisiot"
fi

# Find the version of the package.
PACKAGE_VERSION=$(npm pkg get version --workspaces=false | tr -d \")
printInfo "Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io/esthesisiot" ]]; then
    printError "Cannot push a snapshot version to docker.io/esthesisiot."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64".
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64"
fi

# Set buildx driver options.
if [ -z "$ESTHESIS_BUILDX_KUBERNETES" ]; then
  ESTHESIS_BUILDX_KUBERNETES="false"
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

# Build & Push
IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesis-core-ui"
printInfo "Building $ESTHESIS_ARCHITECTURES for $IMAGE_NAME."
TAGS=("latest" "$PACKAGE_VERSION")
for TAG in "${TAGS[@]}"; do
  printInfo "Building container $IMAGE_NAME:$TAG."
  docker buildx build \
         --platform "$ESTHESIS_ARCHITECTURES" \
         -t "$IMAGE_NAME:$TAG" \
         --push .
done

# Delete the buildx.
printInfo "Deleting Docker buildx $BUILDX_NAME."
docker buildx rm "$BUILDX_NAME"
