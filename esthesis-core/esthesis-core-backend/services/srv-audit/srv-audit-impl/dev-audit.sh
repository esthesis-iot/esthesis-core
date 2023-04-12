#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

./mvnw quarkus:dev \
  -Dquarkus.http.port=59140 \
  -Ddebug=59141 \
  -Dquarkus.profile="$PROFILES"
