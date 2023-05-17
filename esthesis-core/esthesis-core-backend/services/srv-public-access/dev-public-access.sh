#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-public-access-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59160 \
  -Ddebug=59161 \
  -Dquarkus.profile="$PROFILES"
