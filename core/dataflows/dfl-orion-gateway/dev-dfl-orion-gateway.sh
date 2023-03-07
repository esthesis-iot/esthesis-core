#!/usr/bin/env sh

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

env \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_APPLICATION_TOPIC=esthesis-app \
    ESTHESIS_DFL_ORION_URL=http://esthesis-dev-orion:1026 \
    ESTHESIS_DFL_ORION_DEFAULT_TYPE=Device \
    ESTHESIS_DFL_ORION_DELETE_DEVICES=true \
./mvnw quarkus:dev -Ddebug=39239 -Dquarkus.profile="$PROFILES"
