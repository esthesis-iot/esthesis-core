#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Call starter script
export ESTHESIS_DFL_ESTHESIS_DB_URL=mongodb://mongodb.$(kubens -c):27017
export ESTHESIS_DFL_ESTHESIS_DB_NAME=esthesiscore
export ESTHESIS_DFL_ESTHESIS_DB_USERNAME=esthesis-system
export ESTHESIS_DFL_ESTHESIS_DB_PASSWORD=esthesis-system
export ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.$(kubens -c):9092
export ESTHESIS_DFL_KAFKA_COMMAND_REPLY_TOPIC=esthesis-command-reply
export ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-command-reply-updater
export ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT
export ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512
export ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;"
export ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000
export ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10
export ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000
source ../../../_dev/dev-scripts/start-quarkus.sh "." "0" "39150" "$1"
