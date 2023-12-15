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

[ -e "local-env.sh" ] && source "local-env.sh"
cd srv-public-access-impl || exit
./mvnw quarkus:dev \
  -Dquarkus.http.port=59160 \
  -Ddebug=59161 \
  -Dquarkus.profile="$PROFILES" \
	-Dquarkus.console.enabled="$CONSOLE"
