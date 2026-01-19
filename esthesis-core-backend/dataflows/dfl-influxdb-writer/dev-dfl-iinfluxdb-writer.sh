#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# If $ESTHESIS_DFL_INFLUX_TOKEN is not set, ask for it.
if [ -z "$ESTHESIS_DFL_INFLUX_TOKEN" ]; then
	echo "Please enter the InfluxDB token:"
	read -s TOKEN
	export ESTHESIS_DFL_INFLUX_TOKEN=$TOKEN
fi

# Call starter script
export ESTHESIS_DFL_INFLUX_URL=http://influxdb.$(kubens -c):8086
export ESTHESIS_DFL_INFLUX_TOKEN="$ESTHESIS_DFL_INFLUX_TOKEN"
export ESTHESIS_DFL_INFLUX_ORG=esthesis
export ESTHESIS_DFL_INFLUX_BUCKET=esthesis
export ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka-kafka-bootstrap.$(kubens -c):9092
export ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry
export ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata
export ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-influxdb-writer
export ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
export ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512
export ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;"
export ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000
export ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10
export ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000
source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)" \
	MVNW_DIR="$(pwd)/../.." \
	DEBUG_PORT="39151" \
	PROFILES="${1:-dev}${1:+,dev}"
