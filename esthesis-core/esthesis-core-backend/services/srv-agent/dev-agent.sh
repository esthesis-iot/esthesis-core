#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-agent-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59070 \
  -Ddebug=59071 \
  -Dquarkus.profile="$PROFILES"
