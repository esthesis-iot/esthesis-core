#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-campaign-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59150 \
  -Ddebug=59151 \
  -Dquarkus.profile="$PROFILES"
