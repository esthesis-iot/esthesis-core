#!/usr/bin/env sh
./mvnw clean package \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.tag=latest

./mvnw clean package \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true
