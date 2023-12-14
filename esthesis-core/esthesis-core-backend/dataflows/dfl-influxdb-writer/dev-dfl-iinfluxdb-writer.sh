#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

echo Enter influxdb token:
read TOKEN

env \
    ESTHESIS_DFL_INFLUX_URL=http://influxdb.esthesis:8086 \
    ESTHESIS_DFL_INFLUX_TOKEN= $TOKEN \
    ESTHESIS_DFL_INFLUX_ORG=esthesis \
    ESTHESIS_DFL_INFLUX_BUCKET=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-core-kafka-kafka-external-bootstrap.esthesis:9095 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-influxdb-writer \
./mvnw quarkus:dev -Ddebug=39151 -Dquarkus.profile="$PROFILES"
