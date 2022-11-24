#!/usr/bin/env bash

./mvnw quarkus:dev \
  -Dquarkus.http.port=59100 \
  -Ddebug=59101 \
  -Dquarkus.profile=dev
