#!/usr/bin/env bash

####################################################################################################
# Publishes the esthesis-core-device modules to a container registry, and builds native executables.
#
# Environment variables:
#   ESTHESIS_REGISTRY_USERNAME: The username to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_PASSWORD: The password to use when authenticating with the registry.
#   ESTHESIS_REGISTRY_URL: The URL of the registry to push to (default: docker.io).
#		ESTHESIS_BUILD_PROD: If set to true, production images will be built (default: true).
#		ESTHESIS_BUILD_DEBUG: If set to true, debug images will be built (default: true).
#
# Usage examples:
#   ./publish.sh
# 	ESTHESIS_REGISTRY_URL=192.168.10.21:32000 ./publish.sh
# 	ESTHESIS_REGISTRY_URL=192.168.10.21:32000 ESTHESIS_BUILD_PROD=false ./publish.sh
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

rfc3339Date() {
  input=$(date +"%Y-%m-%dT%H:%M:%S%z")
  length=${#input}
  if [ "$length" -ge 2 ]; then
      last_two="${input: -2}"
      rest="${input:0:((length-2))}"
      echo "$rest:$last_two"
  else
      echo "$input"
  fi
}

# Check if Podman is installed.
if [ -x "$(command -v podman)" ]; then
  # Check if Podman machine is running.
  if [ "$(uname)" != "Linux" ]; then
    if ! podman machine inspect &> /dev/null; then
      printError "Podman machine is not running."
      exit 6
    fi
  else
    printInfo "Host is not macOS. Skipping 'podman machine inspect' command."
  fi
else
		printError "Podman is not installed."
		exit 2
fi

# Registry to push to.
PUBLIC_REGISTRY=0
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
	ESTHESIS_REGISTRY_URL="docker.io"
fi
if [[ "$ESTHESIS_REGISTRY_URL" == "docker.io" ]]; then
    PUBLIC_REGISTRY=1
fi

# Builds to execute.
if [ -z "$ESTHESIS_BUILD_PROD" ]; then
	ESTHESIS_BUILD_PROD=true
fi
if [ -z "$ESTHESIS_BUILD_DEBUG" ]; then
	ESTHESIS_BUILD_DEBUG=true
fi

# Find the version of the package.
PACKAGE_VERSION=$(cat go/internal/pkg/config/config.go | grep "const Version" | cut -d'"' -f2)
printInfo "Package version: $PACKAGE_VERSION."
if [[ "${PACKAGE_VERSION}" == *SNAPSHOT && $ESTHESIS_REGISTRY_URL == "docker.io" ]]; then
    printError "Cannot push a snapshot version to docker.io."
    exit 3
fi

# Native executables build
printInfo "Building native executables."
mkdir -p native/"$VERSION"
pushd .
cd go || exit 4

LDFLAGS="-X github.com/esthesis-iot/esthesis-device/internal/pkg/banner.Commit=$(git rev-parse HEAD) -X github.com/esthesis-iot/esthesis-device/internal/pkg/banner.BuildTime=$(rfc3339Date)"

CGO_ENABLED=0 GOOS=darwin GOARCH=amd64 go build -ldflags "$LDFLAGS" -o ../native/$VERSION/esthesis-core-device-darwin-amd64 cmd/main.go
if [ $? -ne 0 ]; then
	printError "Failed to build native executable for darwin/amd64."
	exit 5
fi

CGO_ENABLED=0 GOOS=darwin GOARCH=arm64 go build -ldflags "$LDFLAGS" -o ../native/$VERSION/esthesis-core-device-darwin-arm64 cmd/main.go
if [ $? -ne 0 ]; then
	printError "Failed to build native executable for darwin/arm64."
	exit 6
fi

CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -ldflags "$LDFLAGS" -o ../native/$VERSION/esthesis-core-device-linux-amd64 cmd/main.go
if [ $? -ne 0 ]; then
	printError "Failed to build native executable for linux/amd64."
	exit 7
fi

CGO_ENABLED=0 GOOS=linux GOARCH=arm go build -ldflags "$LDFLAGS" -o ../native/$VERSION/esthesis-core-device-linux-arm cmd/main.go
if [ $? -ne 0 ]; then
	printError "Failed to build native executable for linux/arm."
	exit 8
fi

CGO_ENABLED=0 GOOS=linux GOARCH=arm64 go build -ldflags "$LDFLAGS" -o ../native/$VERSION/esthesis-core-device-linux-arm64 cmd/main.go
if [ $? -ne 0 ]; then
	printError "Failed to build native executable for linux/arm64."
	exit 9
fi

CGO_ENABLED=0 GOOS=windows GOARCH=386 go build -ldflags "$LDFLAGS" -o ../native/$VERSION/esthesis-core-device-win-386.exe cmd/main.go
if [ $? -ne 0 ]; then
	printError "Failed to build native executable for windows/386."
	exit 10
fi

CGO_ENABLED=0 GOOS=windows GOARCH=amd64 go build -ldflags "$LDFLAGS" -o ../native/$VERSION/esthesis-core-device-win-amd64.exe cmd/main.go
if [ $? -ne 0 ]; then
	printError "Failed to build native executable for windows/amd64."
	exit 11
fi

popd || exit 12

IMAGE_TYPES=()
if [ "$ESTHESIS_BUILD_PROD" = true ]; then
	IMAGE_TYPES+=("")
fi
if [ "$ESTHESIS_BUILD_DEBUG" = true ]; then
	IMAGE_TYPES+=("-debug")
fi
for IMAGE_TYPE in "${IMAGE_TYPES[@]}"; do
	# Remove existing manifests and images matching the name of the image we are building.
	IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesisiot/esthesis-core-device"
	for TAG in "latest" "$PACKAGE_VERSION"; do
		if podman manifest exists "$IMAGE_NAME:$TAG$IMAGE_TYPE"; then
			printInfo "Removing existing manifest $IMAGE_NAME:$TAG$IMAGE_TYPE."
			podman manifest rm "$IMAGE_NAME:$TAG$IMAGE_TYPE"
			if [ $? -ne 0 ]; then
					printError "Failed to remove existing manifest $IMAGE_NAME:$TAG$IMAGE_TYPE."
					exit 13
			fi
		fi
		if podman image exists "$IMAGE_NAME:$TAG$IMAGE_TYPE"; then
			printInfo "Removing existing image $IMAGE_NAME:$TAG$IMAGE_TYPE."
			podman rmi "$IMAGE_NAME:$TAG$IMAGE_TYPE"
			if [ $? -ne 0 ]; then
					printError "Failed to remove existing image $IMAGE_NAME:$TAG$IMAGE_TYPE."
					exit 14
			fi
		fi
	done

	# Build containers.
	printInfo "Building container $IMAGE_NAME for linux/amd64$IMAGE_TYPE."
	podman build \
				 --platform "linux/amd64" \
				 -t $IMAGE_NAME:linux-amd64-latest$IMAGE_TYPE \
				 -t $IMAGE_NAME:linux-amd64-$PACKAGE_VERSION$IMAGE_TYPE \
				 -f Containerfile-linux-amd64$IMAGE_TYPE .
	if [ $? -ne 0 ]; then
		printError "Failed to build container $IMAGE_NAME for linux/amd64$IMAGE_TYPE."
		exit 15
	fi

	printInfo "Building container $IMAGE_NAME for linux/arm64$IMAGE_TYPE."
	podman build \
				 --platform "linux/arm64" \
				 -t $IMAGE_NAME:linux-arm64-latest$IMAGE_TYPE \
				 -t $IMAGE_NAME:linux-arm64-$PACKAGE_VERSION$IMAGE_TYPE \
				 -f Containerfile-linux-arm64$IMAGE_TYPE .
	if [ $? -ne 0 ]; then
		printError "Failed to build container $IMAGE_NAME for linux/arm64$IMAGE_TYPE."
		exit 16
	fi

	printInfo "Building container $IMAGE_NAME for linux/arm$IMAGE_TYPE."
	podman build \
				 --platform "linux/arm" \
				 -t $IMAGE_NAME:linux-arm-latest$IMAGE_TYPE \
				 -t $IMAGE_NAME:linux-arm-$PACKAGE_VERSION$IMAGE_TYPE \
				 -f Containerfile-linux-arm$IMAGE_TYPE .
	if [ $? -ne 0 ]; then
		printError "Failed to build container $IMAGE_NAME for linux/arm$IMAGE_TYPE."
		exit 17
	fi

	# Create manifests.
	printInfo "Creating manifest for $IMAGE_NAME:latest$IMAGE_TYPE."
	podman manifest create $IMAGE_NAME:latest$IMAGE_TYPE \
		"docker-daemon:$IMAGE_NAME:linux-amd64-latest$IMAGE_TYPE" \
		"docker-daemon:$IMAGE_NAME:linux-arm64-latest$IMAGE_TYPE" \
		"docker-daemon:$IMAGE_NAME:linux-arm-latest$IMAGE_TYPE"
	if [ $? -ne 0 ]; then
		printError "Failed to create manifest for $IMAGE_NAME:latest$IMAGE_TYPE."
		exit 18
	fi
	printInfo "Creating manifest for $IMAGE_NAME:$PACKAGE_VERSION$IMAGE_TYPE."
	podman manifest create "$IMAGE_NAME:$PACKAGE_VERSION$IMAGE_TYPE" \
		"docker-daemon:$IMAGE_NAME:linux-amd64-$PACKAGE_VERSION$IMAGE_TYPE" \
		"docker-daemon:$IMAGE_NAME:linux-arm64-$PACKAGE_VERSION$IMAGE_TYPE" \
		"docker-daemon:$IMAGE_NAME:linux-arm-$PACKAGE_VERSION$IMAGE_TYPE"
	 if [ $? -ne 0 ]; then
		printError "Failed to create manifest for $IMAGE_NAME:$PACKAGE_VERSION$IMAGE_TYPE."
		exit 19
	 fi

	# Push manifests.
	printInfo "Pushing container manifests."
	CREDS=""
	TLS_VERIFY=""
	if [ "$PUBLIC_REGISTRY" = true ]; then
		CREDS="--creds $ESTHESIS_REGISTRY_USERNAME:$ESTHESIS_REGISTRY_PASSWORD"
	else
		TLS_VERIFY="--tls-verify=false"
	fi
	podman manifest push --all "$IMAGE_NAME:latest$IMAGE_TYPE" $CREDS $TLS_VERIFY --rm
	if [ $? -ne 0 ]; then
		printError "Failed to push manifest for $IMAGE_NAME:latest$IMAGE_TYPE."
		exit 20
	fi
	podman manifest push --all "$IMAGE_NAME:$PACKAGE_VERSION$IMAGE_TYPE" $CREDS $TLS_VERIFY --rm
	if [ $? -ne 0 ]; then
		printError "Failed to push manifest for $IMAGE_NAME:$PACKAGE_VERSION$IMAGE_TYPE."
		exit 21
	fi
done
