#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

env \
    ESTHESIS_DFL_ESTHESIS_DB_URL=mongodb://mongodb.esthesis.localdev:27017 \
    ESTHESIS_DFL_ESTHESIS_DB_NAME=esthesiscore \
    ESTHESIS_DFL_ESTHESIS_DB_USERNAME=esthesis-system \
    ESTHESIS_DFL_ESTHESIS_DB_PASSWORD=esthesis-system \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis.localdev:9094 \
    ESTHESIS_DFL_KAFKA_PING_TOPIC=esthesis-ping \
    ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-ping-updater \
./mvnw quarkus:dev -Ddebug=39154 -Dquarkus.profile="$PROFILES"
