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
    ESTHESIS_DFL_ESTHESIS_DB_URL=mongodb://esthesis-dev-mongodb:27017 \
    ESTHESIS_DFL_ESTHESIS_DB_NAME=esthesis \
    ESTHESIS_DFL_ESTHESIS_DB_USERNAME=esthesis \
    ESTHESIS_DFL_ESTHESIS_DB_PASSWORD=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_PING_TOPIC=esthesis-command-reply \
./mvnw quarkus:dev -Ddebug="$(randomPort)" -Dquarkus.profile=dev
