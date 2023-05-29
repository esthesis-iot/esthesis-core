#!/usr/bin/env bash

# Find the version of the package.
PACKAGE_VERSION=$(npm pkg get version --workspaces=false | tr -d \")
echo "Building esthesisiot/esthesis-core-ui:$PACKAGE_VERSION"

# Architectures to build, i.e. "linux/amd64,linux/arm64"
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64"
fi

# Build using a random buildx.
RANDOM_NAME=$(uuidgen | tr '[:upper:]' '[:lower:]' | tr -d '-' | awk '{if($0~/^[0-9]/)sub(/^[0-9]/,sprintf("%c", 97+rand()*26)); print}')
echo "Creating temporary buildx: $RANDOM_NAME"
{
docker buildx create --name "$RANDOM_NAME" --platform "$ESTHESIS_ARCHITECTURES" --config buildkit-config.tom --use
if [ -z "$ESTHESIS_REGISTRY" ]; then
  docker buildx build -t esthesisiot/esthesis-core-ui:latest -t esthesisiot/esthesis-core-ui:"$PACKAGE_VERSION" --platform $ESTHESIS_ARCHITECTURES --push .
else
  docker buildx build -t "$ESTHESIS_REGISTRY"/esthesisiot/esthesis-core-ui:latest -t "$ESTHESIS_REGISTRY"/esthesisiot/esthesis-core-ui:"$PACKAGE_VERSION" --platform $ESTHESIS_ARCHITECTURES --push .
fi
} || {
  echo "Failed to build esthesisiot/esthesis-core-ui:$PACKAGE_VERSION"
}

# Remove the temporary buildx instance.
docker buildx rm "$RANDOM_NAME"
echo "Removed temporary buildx: $RANDOM_NAME"
