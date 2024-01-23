#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

# If $ESTHESIS_DFL_INFLUX_TOKEN is not set, ask for it.
if [ -z "$ESTHESIS_DFL_INFLUX_TOKEN" ]; then
	echo "Please enter the InfluxDB token:"
	read -s TOKEN
	export ESTHESIS_DFL_INFLUX_TOKEN=$TOKEN
fi

[ -e "local-env.sh" ] && source "local-env.sh"
env \
    ESTHESIS_DFL_INFLUX_URL=http://influxdb.esthesis:8086 \
    ESTHESIS_DFL_INFLUX_TOKEN="$ESTHESIS_DFL_INFLUX_TOKEN" \
    ESTHESIS_DFL_INFLUX_ORG=esthesis \
    ESTHESIS_DFL_INFLUX_BUCKET=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis:9092 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-influxdb-writer \
    ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT \
		ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512 \
		ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;" \
		ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000 \
		ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10 \
		ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000 \
./mvnw quarkus:dev -Ddebug=39151 -Dquarkus.profile="$PROFILES"
