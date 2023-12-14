#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

env \
    ESTHESIS_DFL_ESTHESIS_DB_URL=mongodb://mongodb.esthesis:27017 \
		ESTHESIS_DFL_ESTHESIS_DB_NAME=esthesiscore \
		ESTHESIS_DFL_ESTHESIS_DB_USERNAME=esthesis-system \
		ESTHESIS_DFL_ESTHESIS_DB_PASSWORD=esthesis-system \
		ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-core-kafka-kafka-external-bootstrap.esthesis:9095 \
    ESTHESIS_DFL_KAFKA_COMMAND_REPLY_TOPIC=esthesis-command-reply \
    ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-command-reply-updater \
./mvnw quarkus:dev -Ddebug=39150 -Dquarkus.profile="$PROFILES"
