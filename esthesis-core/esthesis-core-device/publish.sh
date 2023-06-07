#!/usr/bin/env bash

# Find the version of the package.
VERSION=$(cat go/internal/pkg/config/config.go | grep "const Version" | cut -d'"' -f2)
echo "Building esthesisiot/esthesis-core-device:$VERSION"

# Architectures to build, i.e. "linux/amd64,linux/arm64"
if [ -z "$ESTHESIS_ARCHITECTURES" ]; then
	ESTHESIS_ARCHITECTURES="linux/amd64,linux/arm64"
fi

# Build using a random buildx.
RANDOM_NAME=$(uuidgen | tr '[:upper:]' '[:lower:]' | tr -d '-' | awk '{if($0~/^[0-9]/)sub(/^[0-9]/,sprintf("%c", 97+rand()*26)); print}')
echo "Creating temporary buildx: $RANDOM_NAME"
{
docker buildx create --name "$RANDOM_NAME" --config buildkit-config.tom --use
docker buildx inspect "$RANDOM_NAME"
if [ -z "$ESTHESIS_REGISTRY" ]; then
  docker buildx build -t esthesisiot/esthesis-core-device:latest -t esthesisiot/esthesis-core-device:"$VERSION" --platform $ESTHESIS_ARCHITECTURES --push .
else
  docker buildx build -t "$ESTHESIS_REGISTRY"/esthesisiot/esthesis-core-device:latest -t "$ESTHESIS_REGISTRY"/esthesisiot/esthesis-core-device:"$VERSION" --platform $ESTHESIS_ARCHITECTURES --push .
fi
} || {
  echo "***ERROR: Failed to build esthesisiot/esthesis-core-device:$VERSION"
}

# Remove the temporary buildx instance.
docker buildx rm "$RANDOM_NAME"
echo "Removed temporary buildx: $RANDOM_NAME"

# Build native executables to be attached to releases.
echo "Building native executables (will be git-ignored)"
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
	echo "***ERROR: Failed to build native executables"
}
popd || exit
