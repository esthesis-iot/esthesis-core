#!/usr/bin/env bash

./mvnw quarkus:dev \
  -Dquarkus.http.port=59080 \
  -Ddebug=59081 \
  -Dquarkus.profile=dev
