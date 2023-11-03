#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-ui modules to a container registry.
#
# Required environment variables:
#   ESTHESIS_REGISTRY_USERNAME: The username to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_PASSWORD: The password to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_URL (default: docker.io): The URL of the registry to push to.
#   ESTHESIS_ARCHITECTURES (default: linux/amd64): The architectures to build.
#
# Usage:
#   ./publish.sh
####################################################################################################

# Check if Podman is installed.
if [ -x "$(command -v podman)" ]; then
  # Check if Podman machine is running.
  if ! podman machine inspect &> /dev/null; then
    echo "***ERROR Podman machine is not running."
    exit 6
  fi
else
		echo "***ERROR Podman is not installed."
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
echo "***INFO: Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io" ]]; then
    echo "***ERROR Cannot push a snapshot version to docker.io."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64".
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64"
fi
JOBS=$(echo "$ESTHESIS_ARCHITECTURES" | tr ',' '\n' | wc -l | sed -e 's/^[[:space:]]*//')

# Build & Push
IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesisiot/esthesis-core-ui"
echo "***INFO: Building $ESTHESIS_ARCHITECTURES for $IMAGE_NAME."
unset CREDS
if [ -n "$ESTHESIS_REGISTRY_USERNAME" ] && [ -n "$ESTHESIS_REGISTRY_PASSWORD" ]; then
  CREDS="--creds $ESTHESIS_REGISTRY_USERNAME:$ESTHESIS_REGISTRY_PASSWORD"
fi
TAGS=("latest" "$PACKAGE_VERSION")
for TAG in "${TAGS[@]}"; do
  if podman manifest exists "$IMAGE_NAME:$TAG"; then
    echo "***INFO: Removing existing manifest $IMAGE_NAME:$TAG."
    podman manifest rm "$IMAGE_NAME:$TAG"
  fi
  echo "***INFO: Building container $IMAGE_NAME:$TAG."
  podman build \
         --jobs "$JOBS" \
         --platform "$ESTHESIS_ARCHITECTURES" \
         --manifest "$IMAGE_NAME:$TAG" .
  echo "***INFO: Pushing container $IMAGE_NAME:$TAG."
  if [ "$PUBLIC_REGISTRY" = true ]; then
    podman manifest push "$IMAGE_NAME:$TAG" $CREDS --rm
  else
    podman manifest push "$IMAGE_NAME:$TAG" $CREDS --rm --tls-verify=false
  fi
done
