#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

./mvnw quarkus:dev \
  -Dquarkus.http.port=59010 \
  -Ddebug=59011 \
  -Dquarkus.profile="$PROFILES"
