#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

cd srv-about-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59120 \
  -Ddebug=59121 \
  -Dquarkus.profile="$PROFILES"
