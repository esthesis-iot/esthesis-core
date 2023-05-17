#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-provisioning-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59100 \
  -Ddebug=59101 \
  -Dquarkus.profile="$PROFILES"
