#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-infrastructure-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59110 \
  -Ddebug=59111 \
  -Dquarkus.profile="$PROFILES"
