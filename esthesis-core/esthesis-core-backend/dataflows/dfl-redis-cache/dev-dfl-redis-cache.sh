#!/usr/bin/env sh

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

env \
    ESTHESIS_DFL_REDIS_URL=redis://esthesis-dev-redis:6379/0 \
    ESTHESIS_DFL_REDIS_PASSWORD=esthesis \
    ESTHESIS_DFL_REDIS_MAX_SIZE=1024 \
    ESTHESIS_DFL_REDIS_TTL=0 \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-redis-cache \
./mvnw quarkus:dev -Ddebug=39156 -Dquarkus.profile="$PROFILES"
