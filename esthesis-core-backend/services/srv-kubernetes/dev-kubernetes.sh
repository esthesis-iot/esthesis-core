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

if [ -e "$(pwd)/local-env.sh" ]; then
	echo "Sourcing $(pwd)/local-env.sh."
	source "local-env.sh"
fi

cd srv-kubernetes-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59050 \
  -Ddebug=59051 \
  -Dquarkus.profile="$PROFILES" \
	-Dquarkus.console.enabled="$CONSOLE"
