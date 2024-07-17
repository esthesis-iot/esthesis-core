#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.



# Call starter script
export ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.$(kubens -c):9092
export ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
export ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512
export ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;"
export ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry
export ESTHESIS_DFL_KAFKA_APPLICATION_TOPIC=esthesis-app
export ESTHESIS_DFL_ORION_URL=http://orionld.$(kubens -c):1026
export ESTHESIS_DFL_ORION_DEFAULT_TYPE=Device
export ESTHESIS_DFL_ORION_DELETE_DEVICES=true
export ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000
export ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10
export ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)" \
	MVNW_DIR="$(pwd)/../.." \
	DEBUG_PORT="39239" \
	OIDC_CLIENT="true" \
	PROFILES="${1:-dev}${1:+,dev}"

# If running this dfl in Kubernetes in dev mode, you need to add the following custom env var:
# QUARKUS_REST_CLIENT_DEVICESYSTEMRESOURCE_URL=http://...:59010
