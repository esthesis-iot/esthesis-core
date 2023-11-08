#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

CONSOLE=true
if [ "$TERM_PROGRAM" = tmux ]; then
  CONSOLE=false
fi

cd srv-dataflow-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59060 \
  -Ddebug=59061 \
  -Dquarkus.profile="$PROFILES" \
	-Dquarkus.console.enabled="$CONSOLE"
