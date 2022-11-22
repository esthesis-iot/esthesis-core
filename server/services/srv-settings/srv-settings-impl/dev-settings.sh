#!/usr/bin/env bash

./mvnw quarkus:dev \
  -Dquarkus.http.port=59030 \
  -Ddebug=59031 \
  -Dredis.url=redis://esthesis-dev-redis:6379/0 \
  -Dquarkus.profile=dev
