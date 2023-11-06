#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-ui modules to a container registry.
#
# Required environment variables:
#   ESTHESIS_REGISTRY_USERNAME: The username to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_PASSWORD: The password to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_URL: The URL of the registry to push to (default: docker.io).
#   ESTHESIS_ARCHITECTURES: The architectures to build (default: linux/amd64).
#
# Usage:
#   ./publish.sh
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
  # Check if Podman machine is running.
  if ! podman machine inspect &> /dev/null; then
    printError "Podman machine is not running."
    exit 6
  fi
else
		printError "Podman is not installed."
		exit 5
fi

# Registry to push to.
PUBLIC_REGISTRY=0
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
	ESTHESIS_REGISTRY_URL="docker.io"
fi
if [[ "$ESTHESIS_REGISTRY_URL" == "docker.io" ]]; then
    PUBLIC_REGISTRY=1
fi

# Find the version of the package.
PACKAGE_VERSION=$(npm pkg get version --workspaces=false | tr -d \")
printInfo "Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io" ]]; then
    printError "Cannot push a snapshot version to docker.io."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64".
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64"
fi
JOBS=$(echo "$ESTHESIS_ARCHITECTURES" | tr ',' '\n' | wc -l | sed -e 's/^[[:space:]]*//')

# Build & Push
IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesisiot/esthesis-core-ui"
printInfo "Building $ESTHESIS_ARCHITECTURES for $IMAGE_NAME."
unset CREDS
if [ -n "$ESTHESIS_REGISTRY_USERNAME" ] && [ -n "$ESTHESIS_REGISTRY_PASSWORD" ]; then
  CREDS="--creds $ESTHESIS_REGISTRY_USERNAME:$ESTHESIS_REGISTRY_PASSWORD"
fi
TAGS=("latest" "$PACKAGE_VERSION")
for TAG in "${TAGS[@]}"; do
  if podman manifest exists "$IMAGE_NAME:$TAG"; then
    printInfo "Removing existing manifest $IMAGE_NAME:$TAG."
    podman manifest rm "$IMAGE_NAME:$TAG"
  fi
  printInfo "Building container $IMAGE_NAME:$TAG."
  podman build \
         --jobs "$JOBS" \
         --platform "$ESTHESIS_ARCHITECTURES" \
         --manifest "$IMAGE_NAME:$TAG" .
  printInfo "Pushing container $IMAGE_NAME:$TAG."
  if [ "$PUBLIC_REGISTRY" = true ]; then
    podman manifest push "$IMAGE_NAME:$TAG" $CREDS --rm
  else
    podman manifest push "$IMAGE_NAME:$TAG" $CREDS --rm --tls-verify=false
  fi
done
