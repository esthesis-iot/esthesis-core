#!/usr/bin/env sh

cd srv-infrastructure-impl && \
./mvnw clean package \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.additional-tags=latest && \
cd ..
