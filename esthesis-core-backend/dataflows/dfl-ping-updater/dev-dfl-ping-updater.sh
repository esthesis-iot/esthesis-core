#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Call starter script
export ESTHESIS_DFL_ESTHESIS_DB_URL=mongodb://mongodb-rs0.$(kubens -c):27017
export ESTHESIS_DFL_ESTHESIS_DB_NAME=esthesiscore
export ESTHESIS_DFL_ESTHESIS_DB_USERNAME=esthesis-system
export ESTHESIS_DFL_ESTHESIS_DB_PASSWORD=esthesis-system
export ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka-kafka-bootstrap.$(kubens -c):9092
export ESTHESIS_DFL_KAFKA_PING_TOPIC=esthesis-ping
export ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-ping-updater
export ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
export ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512
export ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;"
export ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000
export ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10
export ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000
source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)" \
	MVNW_DIR="$(pwd)/../.." \
	DEBUG_PORT="39154" \
	PROFILES="${1:-dev}${1:+,dev}"
