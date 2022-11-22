#!/usr/bin/env bash

./mvnw quarkus:dev \
  -Dquarkus.http.port=59050 \
  -Ddebug=59051 \
  -Dquarkus.profile=dev
