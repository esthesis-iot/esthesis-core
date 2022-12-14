#!/usr/bin/env sh

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

env \
    ESTHESIS_DFL_ESTHESIS_DB_URL=mongodb://esthesis-dev-mongodb:27017 \
    ESTHESIS_DFL_ESTHESIS_DB_NAME=esthesis \
    ESTHESIS_DFL_ESTHESIS_DB_USERNAME=esthesis \
    ESTHESIS_DFL_ESTHESIS_DB_PASSWORD=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_COMMAND_REPLY_TOPIC=esthesis-command-reply \
./mvnw quarkus:dev -Ddebug=39150 -Dquarkus.profile="$PROFILES"
