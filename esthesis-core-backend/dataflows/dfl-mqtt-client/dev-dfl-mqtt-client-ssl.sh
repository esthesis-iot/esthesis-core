#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Check that certificates and private key are present before running this script.
if [ ! -f "certs/esthesis-core-root-ca.crt" ] || [ ! -f "certs/esthesis-platform.crt" ] || [ ! -f "certs/esthesis-platform.key" ]; then
	echo "***ERROR: Certificates and private key are missing. You may create the following files in the 'certs' directory:"
	echo "  - esthesis-core-root-ca.crt"
	echo "  - esthesis-platform.crt"
	echo "  - esthesis-platform.key"
	exit 1
fi

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
export ESTHESIS_DFL_MQTT_BROKER_CA=certs/esthesis-core-root-ca.crt
export ESTHESIS_DFL_MQTT_BROKER_CERT=certs/esthesis-platform.crt
export ESTHESIS_DFL_MQTT_BROKER_KEY=certs/esthesis-platform.key
source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)" \
	MVNW_DIR="$(pwd)/../.." \
	DEBUG_PORT="39152" \
	PROFILES="${1:-dev}${1:+,dev}"

# Trying to debug TLS related issues? Try this:
# -Djavax.net.debug=ssl,handshake
