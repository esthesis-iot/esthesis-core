#!/usr/bin/env sh

PACKAGE_VERSION=$(npm pkg get version --workspaces=false | tr -d \")
echo "Building esthesisiot/esthesis-core-ui:$PACKAGE_VERSION"

DOCKER_BUILDKIT=1 docker build -t esthesisiot/esthesis-core-ui:latest -t esthesisiot/esthesis-core-ui:"$PACKAGE_VERSION" .
docker push esthesisiot/esthesis-core-ui:"$PACKAGE_VERSION"
docker push esthesisiot/esthesis-core-ui:latest
