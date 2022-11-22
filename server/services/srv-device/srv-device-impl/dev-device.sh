#!/usr/bin/env bash

./mvnw quarkus:dev \
  -Dquarkus.http.port=59010 \
  -Ddebug=59011 \
  -Dredis.url=redis://esthesis-dev-redis:6379/0 \
  -Dquarkus.profile=dev
