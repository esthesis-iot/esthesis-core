#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

[ -e "local-env.sh" ] && source "local-env.sh"
env \
    ESTHESIS_DFL_MQTT_TOPIC_PING=esthesis/ping \
    ESTHESIS_DFL_MQTT_TOPIC_TELEMETRY=esthesis/telemetry \
    ESTHESIS_DFL_MQTT_TOPIC_METADATA=esthesis/metadata \
    ESTHESIS_DFL_MQTT_TOPIC_COMMAND_REQUEST=esthesis/command/request \
    ESTHESIS_DFL_MQTT_TOPIC_COMMAND_REPLY=esthesis/command/reply \
    ESTHESIS_DFL_KAFKA_TOPIC_PING=esthesis-ping \
    ESTHESIS_DFL_KAFKA_TOPIC_TELEMETRY=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_TOPIC_METADATA=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_TOPIC_COMMAND_REQUEST=esthesis-command-request \
    ESTHESIS_DFL_KAFKA_TOPIC_COMMAND_REPLY=esthesis-command-reply \
    ESTHESIS_DFL_MQTT_BROKER_CLUSTER_URL=tcp://mosquitto.esthesis:1883 \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis:9092 \
    ESTHESIS_DFL_KAFKA_SECURITY_PROTOCOL=SASL_PLAINTEXT \
		ESTHESIS_DFL_KAFKA_SASL_MECHANISM=SCRAM-SHA-512 \
		ESTHESIS_DFL_KAFKA_JAAS_CONFIG="org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;" \
./mvnw quarkus:dev -Ddebug=39152 -Dquarkus.profile="$PROFILES"
