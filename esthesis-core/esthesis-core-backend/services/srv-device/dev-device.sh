#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-device-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59010 \
  -Ddebug=59011 \
  -Dquarkus.profile="$PROFILES"
