#!/usr/bin/env sh

PROFILES="dev"
if [ "$1" != "" ]; then
  PROFILES="$PROFILES,$1"
  echo "Activating profiles: $PROFILES"
fi

env \
    ESTHESIS_DFL_DB_KIND=mysql \
    ESTHESIS_DFL_DB_USERNAME=esthesis \
    ESTHESIS_DFL_DB_PASSWORD=esthesis \
    ESTHESIS_DFL_DB_JDBC_URL="jdbc:mysql://esthesis-dev-mysql:3306/esthesis?useSSL=false&useUnicode=true&characterEncoding=UTF-8&connectionTimeZone=Etc/UTC" \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_GROUP=dfl-rdbms-writer \
    ESTHESIS_DFL_DB_STORAGE_STRATEGY=SINGLE \
./mvnw quarkus:dev -Ddebug=39155 -Dquarkus.profile="$PROFILES"
