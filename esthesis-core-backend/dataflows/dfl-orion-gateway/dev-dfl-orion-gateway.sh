#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

[ -e "local-env.sh" ] && source "local-env.sh"
env ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis:9092 \
		ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT \
    ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512 \
    ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;" \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_APPLICATION_TOPIC=esthesis-app \
    ESTHESIS_DFL_ORION_URL=http://orion.esthesis.localdev:1026 \
    ESTHESIS_DFL_ORION_DEFAULT_TYPE=Device \
    ESTHESIS_DFL_ORION_DELETE_DEVICES=true \
    ESTHESIS_DFL_CONCURRENCY_POLL_TIMEOUT=1000 \
		ESTHESIS_DFL_CONCURRENCY_CONSUMERS=10 \
		ESTHESIS_DFL_CONCURRENCY_QUEUE_SIZE=1000 \
./mvnw quarkus:dev -Ddebug=39239 -Dquarkus.profile="$PROFILES"

# If running this dfl in Kubernetes in dev mode, you need to add the following custom env var:
# QUARKUS_REST_CLIENT_DEVICESYSTEMRESOURCE_URL=http://...:59010
