#!/usr/bin/env bash

./mvnw quarkus:dev \
  -Dquarkus.http.port=59060 \
  -Ddebug=59061 \
  -Dquarkus.profile=dev
