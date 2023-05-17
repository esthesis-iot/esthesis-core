#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-kubernetes-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59050 \
  -Ddebug=59051 \
  -Dquarkus.profile="$PROFILES"
