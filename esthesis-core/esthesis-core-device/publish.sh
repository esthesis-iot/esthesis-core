#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-device modules to a container registry, and builds native executables.
#
# Environment variables:
#   ESTHESIS_REGISTRY_USERNAME: The username to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_PASSWORD: The password to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_URL: The URL of the registry to push to (default: docker.io).
#   ESTHESIS_ARCHITECTURES: The architectures to build (default: linux/amd64).
#		ESTHESIS_BUILD_DOCKER: Whether to build the Docker image (default: true).
#		ESTHESIS_BUILD_NATIVE: Whether to build the native executables (default: true).
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

# Set default build targets.
if [ -z "$ESTHESIS_BUILD_DOCKER" ]; then
	ESTHESIS_BUILD_DOCKER=true
fi
if [ -z "$ESTHESIS_BUILD_NATIVE" ]; then
	ESTHESIS_BUILD_NATIVE=true
fi

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
PACKAGE_VERSION=$(cat go/internal/pkg/config/config.go | grep "const Version" | cut -d'"' -f2)
printInfo "Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io" ]]; then
    printError "Cannot push a snapshot version to docker.io."
    exit 1
fi

# Architectures to build, i.e. "linux/amd64,linux/arm64"
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64,linux/arm64"
fi

# **************************************************************************************************
# Containers builds
# **************************************************************************************************
if [ "$ESTHESIS_BUILD_DOCKER" = true ]; then
	# Build & Push
	IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesisiot/esthesis-core-device"
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
					 --jobs 1 \
					 --platform "$ESTHESIS_ARCHITECTURES" \
					 --manifest "$IMAGE_NAME:$TAG" .
		printInfo "Pushing container $IMAGE_NAME:$TAG."
		if [ "$PUBLIC_REGISTRY" = true ]; then
			podman manifest push "$IMAGE_NAME:$TAG" $CREDS --rm
		else
			podman manifest push "$IMAGE_NAME:$TAG" $CREDS --rm --tls-verify=false
		fi
	done
fi

# **************************************************************************************************
# Native executables build
# **************************************************************************************************
if [ "$ESTHESIS_BUILD_NATIVE" = true ]; then
	printInfo "Building native executables (will be git-ignored)"
	mkdir -p native/"$VERSION"
	pushd .
	cd go || exit
	{
	GOOS=darwin GOARCH=amd64 go build -o ../native/$VERSION/esthesis-agent_darwin-amd64 cmd/main.go
	GOOS=darwin GOARCH=arm64 go build -o ../native/$VERSION/esthesis-agent_darwin-arm64 cmd/main.go
	GOOS=linux GOARCH=amd64 go build -o ../native/$VERSION/esthesis-agent_linux-amd64 cmd/main.go
	GOOS=linux GOARCH=arm go build -o ../native/$VERSION/esthesis-agent_linux-arm cmd/main.go
	GOOS=linux GOARCH=arm64 go build -o ../native/$VERSION/esthesis-agent_linux-arm64 cmd/main.go
	GOOS=windows GOARCH=386 go build -o ../native/$VERSION/esthesis-agent_win-386.exe cmd/main.go
	GOOS=windows GOARCH=amd64 go build -o ../native/$VERSION/esthesis-agent_win-amd64.exe cmd/main.go
	} || {
		printInfo "Failed to build native executables"
	}
	popd || exit
fi
