#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Call starter script
export ESTHESIS_DFL_MQTT_PING_TOPIC=esthesis/ping
export ESTHESIS_DFL_MQTT_TELEMETRY_TOPIC=esthesis/telemetry
export ESTHESIS_DFL_MQTT_METADATA_TOPIC=esthesis/metadata
export ESTHESIS_DFL_MQTT_COMMAND_REQUEST_TOPIC=esthesis/command/request
export ESTHESIS_DFL_MQTT_COMMAND_REPLY_TOPIC=esthesis/command/reply
export ESTHESIS_DFL_MQTT_BROKER_KEEP_ALIVE_INTERVAL=30
export ESTHESIS_DFL_KAFKA_PING_TOPIC=esthesis-ping
export ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry
export ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata
export ESTHESIS_DFL_KAFKA_COMMAND_REQUEST_TOPIC=esthesis-command-request
export ESTHESIS_DFL_KAFKA_COMMAND_REPLY_TOPIC=esthesis-command-reply
export ESTHESIS_DFL_MQTT_BROKER_CLUSTER_URL=tcp://mosquitto.$(kubens -c):1883
export ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka-kafka-bootstrap.$(kubens -c):9092
export ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
export ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512
export ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;"
source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)" \
	MVNW_DIR="$(pwd)/../.." \
	DEBUG_PORT="39152" \
	PROFILES="${1:-dev}${1:+,dev}"

