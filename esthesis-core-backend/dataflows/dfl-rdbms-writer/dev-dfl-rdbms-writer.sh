#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

[ -e "local-env.sh" ] && source "local-env.sh"
env \
    ESTHESIS_DFL_DB_KIND=mysql \
    ESTHESIS_DFL_DB_USERNAME=esthesis-system \
    ESTHESIS_DFL_DB_PASSWORD=esthesis-system \
    ESTHESIS_DFL_DB_JDBC_URL="jdbc:mysql://mysql.esthesis:3306/esthesiscore?useSSL=false&useUnicode=true&characterEncoding=UTF-8&connectionTimeZone=Etc/UTC" \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis:9092 \
    ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT \
		ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512 \
		ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;" \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_GROUP=dfl-rdbms-writer \
    ESTHESIS_DFL_DB_STORAGE_STRATEGY=SINGLE \
    ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000 \
		ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10 \
		ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000 \
./mvnw quarkus:dev -Ddebug=39155 -Dquarkus.profile="$PROFILES"
