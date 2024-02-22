#!/usr/bin/env bash
####################################################################################################
# Publishes the esthesis-core-device modules to a container registry, and builds native executables.
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
#   ESTHESIS_BUILD_NATIVE: 			If set to true, native executables will be built (default: true).
#   ESTHESIS_BUILD_CONTAINERS: 	If set to true, containers will be built and pushed (default: true).
#
# Usage examples:
#   ./publish.sh
# 	ESTHESIS_REGISTRY_URL=192.168.10.21:32000 ./publish.sh
####################################################################################################

# Trap exit.
set -e
exit_handler() {
    printError "Build failed with exit code $?"
    if [ -n "$BUILDX_NAME" ]; then
    				printInfo "Deleting Docker buildx $BUILDX_NAME."
    				docker buildx rm "$BUILDX_NAME"
    		fi
		if [ -n "$CONTAINER_NAME" ]; then
				printInfo "Deleting Docker container $CONTAINER_NAME."
				docker rm "$CONTAINER_NAME"
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

# A date in RFC3339 format.
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

# Set defaults and validate environment variables.
PACKAGE_VERSION=$(cat go/internal/pkg/config/config.go | grep "const Version" | cut -d'"' -f2)
COMMIT_ID=$(git rev-parse HEAD)
BUILD_DATE=$(rfc3339Date)

# If $ESTHESIS_REGISTRY_URL is empty, set it to aws.
if [ -z "$ESTHESIS_REGISTRY_URL" ]; then
  ESTHESIS_REGISTRY_URL="docker.io/esthesisiot"
fi

# If $ESTHESIS_REGISTRY_TYPE is empty, set it to aws.
if [ -z "$ESTHESIS_REGISTRY_TYPE" ]; then
  ESTHESIS_REGISTRY_TYPE="auth"
fi

# Builds to execute.
if [ -z "$ESTHESIS_BUILD_NATIVE" ]; then
	ESTHESIS_BUILD_NATIVE="true"
fi
if [ -z "$ESTHESIS_BUILD_CONTAINERS" ]; then
	ESTHESIS_BUILD_CONTAINERS="true"
fi

printInfo "Package version: $PACKAGE_VERSION."
printInfo "Commit ID: $COMMIT_ID."
printInfo "Build date: $BUILD_DATE."
LDFLAGS="-X github.com/esthesis-iot/esthesis-device/internal/pkg/banner.Commit=$(git rev-parse HEAD) -X github.com/esthesis-iot/esthesis-device/internal/pkg/banner.BuildTime=$(rfc3339Date)"

# Build native images.
if [ "$ESTHESIS_BUILD_NATIVE" = "true" ]; then
	printInfo "Building native images."
	mkdir -p native
	# OS, Architecture, ARM version, extension
	IMAGES=(
		"darwin" 	"amd64" 	"" 	""
		"darwin" 	"arm64" 	"" 	""
		"linux" 	"amd64" 	"" 	""
		"linux" 	"arm" 		"5"	""
		"linux" 	"arm" 		"6"	""
		"linux" 	"arm" 		"7"	""
		"linux" 	"arm64" 	""	""
		"windows" "386" 		""	".exe"
		"windows" "amd64" 	""	".exe"
	)
	for ((i = 0; i < ${#IMAGES[@]}; i += 4)); do
		os=${IMAGES[i]}
		arch=${IMAGES[i + 1]}
		arm_version=${IMAGES[i + 2]}
		extension=${IMAGES[i + 3]}
		printInfo "Building native image for $os/$arch$arm_version"
		CONTAINER_NAME=$(LC_CTYPE=C tr -dc 'a-zA-Z' < /dev/urandom | head -c 1)$(LC_CTYPE=C tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 11)
		docker run \
			-v "$(pwd)/go":/app:ro \
			--name "$CONTAINER_NAME" \
			-e "LDFLAGS=$LDFLAGS" \
			-e "BUILD_DATE=$BUILD_DATE" \
			-e "COMMIT_ID=$COMMIT_ID" \
			-e "VERSION=$PACKAGE_VERSION" \
			-e "CGO_ENABLED=0" \
			-e "GOOS=$os" \
			-e "GOARCH=$arch" \
			-e "GOARM=$arm_version" \
			golang:1.21.3-bookworm \
			/bin/bash -c \
			"cd /app && go build -ldflags \"$LDFLAGS\" \
					-o /native/$PACKAGE_VERSION/esthesis-core-device-$os-$arch$arm_version$extension \
					cmd/main.go && ls -asl /native/$PACKAGE_VERSION"
		docker cp "$CONTAINER_NAME:/native/$PACKAGE_VERSION/esthesis-core-device-$os-$arch$arm_version$extension" \
			native/esthesis-core-device-$os-$arch$arm_version$extension
		docker rm "$CONTAINER_NAME"
	done
fi

if [ "$ESTHESIS_BUILD_CONTAINERS" = "true" ]; then
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

	# OS, Architecture, ARM version, extension
	IMAGE_NAME="$ESTHESIS_REGISTRY_URL/esthesis-core-device"
	printInfo "Building container $IMAGE_NAME:$PACKAGE_VERSION."
	docker buildx build \
		--platform "linux/arm/v6,linux/arm/v7,linux/arm64,linux/amd64" \
		-t "$IMAGE_NAME:$PACKAGE_VERSION" \
		-t "$IMAGE_NAME:latest" \
		--build-arg "LDFLAGS=$LDFLAGS" \
		--push .

	# Delete the buildx.
		printInfo "Deleting Docker buildx $BUILDX_NAME."
		docker buildx rm "$BUILDX_NAME"
fi
