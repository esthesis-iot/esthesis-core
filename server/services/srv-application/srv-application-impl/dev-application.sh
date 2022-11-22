#!/usr/bin/env bash

./mvnw quarkus:dev \
  -Dquarkus.http.port=59090 \
  -Ddebug=59091 \
  -Dredis.url=redis://esthesis-dev-redis:6379/0 \
  -Dquarkus.profile=dev
