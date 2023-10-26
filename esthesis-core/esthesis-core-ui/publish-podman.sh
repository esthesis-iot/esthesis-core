#!/usr/bin/env bash

# Registry to push to.
if [ -z "$ESTHESIS_REGISTRY" ]; then
	ESTHESIS_REGISTRY="docker.io"
fi

# Find the version of the package.
PACKAGE_VERSION=$(npm pkg get version --workspaces=false | tr -d \")
echo "Building $ESTHESIS_REGISTRY/esthesisiot/test:$PACKAGE_VERSION."
#if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY == "docker.io" ]]; then
#    echo "***ERROR Cannot push a snapshot version to docker.io."
#    exit 1
#fi

# Architectures to build, i.e. "linux/amd64,linux/arm64".
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64"
fi
echo "Building for $ESTHESIS_ARCHITECTURES."

# Build & Push
{
  podman build \
         --platform "$ESTHESIS_ARCHITECTURES" \
         --manifest "$ESTHESIS_REGISTRY"/esthesisiot/esthesis-core-ui:latest .
  podman manifest push "$ESTHESIS_REGISTRY"/esthesisiot/esthesis-core-ui:latest

  podman build \
         --platform "$ESTHESIS_ARCHITECTURES" \
         --manifest "$ESTHESIS_REGISTRY"/esthesisiot/esthesis-core-ui:"$PACKAGE_VERSION" .
  podman manifest push "$ESTHESIS_REGISTRY"/esthesisiot/esthesis-core-ui:"$PACKAGE_VERSION"
} || {
   echo "Failed to build $ESTHESIS_REGISTRY/esthesis-core-ui:$PACKAGE_VERSION"
   exit 2
}
