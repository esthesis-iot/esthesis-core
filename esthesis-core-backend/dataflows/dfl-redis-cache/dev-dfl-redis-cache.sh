#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

[ -e "local-env.sh" ] && source "local-env.sh"
env \
    ESTHESIS_DFL_REDIS_URL=redis://redis-master.esthesis:6379/0 \
    ESTHESIS_DFL_REDIS_PASSWORD=esthesis-system \
    ESTHESIS_DFL_REDIS_MAX_SIZE=1024 \
    ESTHESIS_DFL_REDIS_TTL=0 \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis.esthesis:9095 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-redis-cache \
./mvnw quarkus:dev -Ddebug=39156 -Dquarkus.profile="$PROFILES"
