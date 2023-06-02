#!/usr/bin/env bash

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

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
    ESTHESIS_DFL_MQTT_BROKER_CLUSTER_URL=tcp://mqtt.esthesis.localdev:1883 \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis.localdev:9094 \
./mvnw quarkus:dev -Ddebug=39152 -Dquarkus.profile="$PROFILES"
