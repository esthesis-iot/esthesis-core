#!/usr/bin/env sh

randomPort() {
    echo netstat -aln | awk '
      $6 == "LISTEN" {
        if ($4 ~ "[.:][0-9]+$") {
          split($4, a, /[:.]/);
          port = a[length(a)];
          p[port] = 1
        }
      }
      END {
        for (i = 3000; i < 65000 && p[i]; i++){};
        if (i == 65000) {exit 1};
        print i
      }
    '
}

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
./mvnw quarkus:dev -Ddebug="$(randomPort)" -Dquarkus.profile=dev
