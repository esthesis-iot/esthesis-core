#!/usr/bin/env sh

env \
    ESTHESIS_DFL_MQTT_TOPIC_PING=esthesis/ping \
    ESTHESIS_DFL_MQTT_TOPIC_TELEMETRY=esthesis/telemetry \
    ESTHESIS_DFL_MQTT_TOPIC_METADATA=esthesis/metadata \
    ESTHESIS_DFL_MQTT_TOPIC_CONTROL_REQUEST=esthesis/control/request \
    ESTHESIS_DFL_MQTT_TOPIC_CONTROL_REPLY=esthesis/control/reply \
    ESTHESIS_DFL_KAFKA_TOPIC_PING=esthesis-ping \
    ESTHESIS_DFL_KAFKA_TOPIC_TELEMETRY=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_TOPIC_METADATA=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_TOPIC_CONTROL_REQUEST=esthesis-control-request \
    ESTHESIS_DFL_KAFKA_TOPIC_CONTROL_REPLY=esthesis-control-reply \
    ESTHESIS_DFL_MQTT_BROKER_CLUSTER_URL=tcp://esthesis-dev-rabbitmq:1883 \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
./mvnw quarkus:dev -Dquarkus.profile=dev
