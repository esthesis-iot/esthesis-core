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
    ESTHESIS_DFL_MQTT_BROKER_CLUSTER_URL=ssl://mosquitto.esthesis:8883 \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=kafka.esthesis=:9095 \
    ESTHESIS_DFL_MQTT_BROKER_CA=certs/esthesis-core-root-ca.crt \
    ESTHESIS_DFL_MQTT_BROKER_CERT=certs/esthesis-platform.crt \
    ESTHESIS_DFL_MQTT_BROKER_KEY=certs/esthesis-platform.key \
./mvnw quarkus:dev -Ddebug=39152 -Dquarkus.profile="$PROFILES"

# Trying to debug TLS related issues? Try this:
# ./mvnw quarkus:dev -Djavax.net.debug=ssl,handshake -Ddebug=39152 -Dquarkus.profile="$PROFILES"
