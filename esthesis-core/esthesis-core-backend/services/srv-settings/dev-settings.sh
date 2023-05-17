#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-settings-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59030 \
  -Ddebug=59031 \
  -Dredis.url=redis://esthesis-dev-redis:6379/0 \
  -Dquarkus.profile="$PROFILES"
