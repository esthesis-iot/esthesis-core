#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-ui modules to a container registry.
#
# Required environment variables:
#   ESTHESIS_REGISTRY_URL: 			The URL of the registry to push to
#   														(default: public.ecr.aws/b0c5e0h9).
#  	ESTHESIS_REGISTRY_TYPE:			aws: Login will be attempted using 'aws ecr-public get-login-password'.
#  															auth: Login will be attempted using username and password.
#  															open:	No login will be attempted.
#  															(default: aws).
#   ESTHESIS_REGISTRY_USERNAME:	The username to login to the 'auth' type registry.
#   ESTHESIS_REGISTRY_PASSWORD:	The password to login to the 'auth' type registry.
#   ESTHESIS_ARCHITECTURES: 		The architectures to build, e.g. linux/amd64,linux/arm64
#   														(default: linux/amd64,linux/arm64).
#
# Usage:
#   ./publish.sh
####################################################################################################

# Trap exit.
set -e
exit_handler() {
    echo "*** ERROR: Build failed with exit code $?"
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

# If $ESTHESIS_REGISTRY_URL is empty, set it to aws.
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
  ESTHESIS_REGISTRY_URL="public.ecr.aws/b0c5e0h9"
fi

# If $ESTHESIS_REGISTRY_TYPE is empty, set it to aws.
if [ -z "$ESTHESIS_REGISTRY_TYPE" ]; then
  ESTHESIS_REGISTRY_TYPE="aws"
fi

# Find the version of the package.
PACKAGE_VERSION=$(npm pkg get version --workspaces=false | tr -d \")
printInfo "Package version: $PACKAGE_VERSION"
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "public.ecr.aws/b0c5e0h9" ]]; then
    printError "Cannot push a snapshot version to docker.io/esthesisiot."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64".
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64,linux/arm64"
fi

# Login to remote registry.
if [ "$ESTHESIS_REGISTRY_TYPE" = "aws" ]; then
	aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin $ESTHESIS_REGISTRY_URL
elif [ "$ESTHESIS_REGISTRY_TYPE" = "auth" ]; then
	docker login "$ESTHESIS_REGISTRY_URL" --username "$ESTHESIS_REGISTRY_USERNAME" --password "$ESTHESIS_REGISTRY_PASSWORD"
fi

# Build & Push
IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesis-core-ui"
printInfo "Building $ESTHESIS_ARCHITECTURES for $IMAGE_NAME:$PACKAGE_VERSION"
docker buildx build \
       --platform "$ESTHESIS_ARCHITECTURES" \
       -t "$IMAGE_NAME:$PACKAGE_VERSION" \
       -t "$IMAGE_NAME:latest" \
       --push .

