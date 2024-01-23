#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

[ -e "local-env.sh" ] && source "local-env.sh"
env \
    ESTHESIS_DFL_ESTHESIS_DB_URL=mongodb://mongodb-headless.esthesis:27017 \
		ESTHESIS_DFL_ESTHESIS_DB_NAME=esthesiscore \
		ESTHESIS_DFL_ESTHESIS_DB_USERNAME=esthesis-system \
		ESTHESIS_DFL_ESTHESIS_DB_PASSWORD=esthesis-system \
		ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis:9092 \
    ESTHESIS_DFL_KAFKA_COMMAND_REPLY_TOPIC=esthesis-command-reply \
    ESTHESIS_DFL_KAFKA_CONSUMER_GROUP=dfl-command-reply-updater \
    ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT \
		ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512 \
		ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;" \
		ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000 \
		ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10 \
		ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000 \
./mvnw quarkus:dev -Ddebug=39150 -Dquarkus.profile="$PROFILES"
