#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-command-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59080 \
  -Ddebug=59081 \
  -Dquarkus.profile="$PROFILES"
