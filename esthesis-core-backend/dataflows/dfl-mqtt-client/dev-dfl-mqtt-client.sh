#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Call starter script
export ESTHESIS_DFL_MQTT_TOPIC_PING=esthesis/ping
export ESTHESIS_DFL_MQTT_TOPIC_TELEMETRY=esthesis/telemetry
export ESTHESIS_DFL_MQTT_TOPIC_METADATA=esthesis/metadata
export ESTHESIS_DFL_MQTT_TOPIC_COMMAND_REQUEST=esthesis/command/request
export ESTHESIS_DFL_MQTT_TOPIC_COMMAND_REPLY=esthesis/command/reply
export ESTHESIS_DFL_KAFKA_TOPIC_PING=esthesis-ping
export ESTHESIS_DFL_KAFKA_TOPIC_TELEMETRY=esthesis-telemetry
export ESTHESIS_DFL_KAFKA_TOPIC_METADATA=esthesis-metadata
export ESTHESIS_DFL_KAFKA_TOPIC_COMMAND_REQUEST=esthesis-command-request
export ESTHESIS_DFL_KAFKA_TOPIC_COMMAND_REPLY=esthesis-command-reply
export ESTHESIS_DFL_MQTT_BROKER_CLUSTER_URL=tcp://mosquitto.$(kubens -c):1883
export ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.$(kubens -c):9092
export ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
export ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512
export ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;"
source ../../../_dev/dev-scripts/start-quarkus.sh "." "0" "39152" "$1"
