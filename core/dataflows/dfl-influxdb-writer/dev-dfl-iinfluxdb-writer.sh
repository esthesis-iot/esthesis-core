#!/usr/bin/env sh

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

env \
    ESTHESIS_DFL_INFLUX_URL=http://esthesis-dev-influxdb:8086 \
    ESTHESIS_DFL_INFLUX_TOKEN=Orlx1ELFS6_cBucJKVq4reftpB9_maMJMGKSAKOAIj2sVkF8ysowQLGUMhzj7yuhTuhMqhBzPXmU1xh9o60dKA== \
    ESTHESIS_DFL_INFLUX_ORG=esthesis \
    ESTHESIS_DFL_INFLUX_BUCKET=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_GROUP=dfl-influxdb-writer \
./mvnw quarkus:dev -Ddebug=39151 -Dquarkus.profile="$PROFILES"
