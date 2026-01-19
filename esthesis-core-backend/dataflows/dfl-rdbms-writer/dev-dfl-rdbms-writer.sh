#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Call starter script
export ESTHESIS_DFL_DB_KIND=mysql
export ESTHESIS_DFL_DB_USERNAME=esthesis-system
export ESTHESIS_DFL_DB_PASSWORD=esthesis-system
export ESTHESIS_DFL_DB_JDBC_URL="jdbc:mysql://mysql.$(kubens -c):3306/esthesiscore?useSSL=false&useUnicode=true&characterEncoding=UTF-8&connectionTimeZone=Etc/UTC"
export ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka-kafka-bootstrap.$(kubens -c):9092
export ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
export ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512
export ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;"
export ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry
export ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata
export ESTHESIS_DFL_KAFKA_GROUP=dfl-rdbms-writer
export ESTHESIS_DFL_DB_STORAGE_STRATEGY=SINGLE
export ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000
export ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10
export ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000
source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)" \
	MVNW_DIR="$(pwd)/../.." \
	DEBUG_PORT="39155" \
	PROFILES="${1:-dev}${1:+,dev}"
