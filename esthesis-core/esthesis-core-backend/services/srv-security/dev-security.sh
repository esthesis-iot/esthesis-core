#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-security-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59170 \
  -Ddebug=59171 \
  -Dquarkus.profile="$PROFILES"
