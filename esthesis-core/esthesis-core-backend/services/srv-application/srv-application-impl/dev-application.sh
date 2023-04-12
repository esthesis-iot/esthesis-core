#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

./mvnw quarkus:dev \
  -Dquarkus.http.port=59090 \
  -Ddebug=59091 \
  -Dredis.url=redis://esthesis-dev-redis:6379/0 \
  -Dquarkus.profile="$PROFILES"
