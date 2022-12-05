#!/usr/bin/env bash

./mvnw quarkus:dev \
  -Dquarkus.http.port=59010 \
  -Ddebug=59011 \
  -Dquarkus.profile=dev
