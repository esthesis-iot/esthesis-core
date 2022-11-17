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
    ESTHESIS_DFL_INFLUX_URL=http://esthesis-dev-influxdb:8086 \
    ESTHESIS_DFL_INFLUX_TOKEN=Orlx1ELFS6_cBucJKVq4reftpB9_maMJMGKSAKOAIj2sVkF8ysowQLGUMhzj7yuhTuhMqhBzPXmU1xh9o60dKA== \
    ESTHESIS_DFL_INFLUX_ORG=esthesis \
    ESTHESIS_DFL_INFLUX_BUCKET=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_GROUP=dfl-influxdb-writer \
./mvnw quarkus:dev -Ddebug="$(randomPort)" -Dquarkus.profile=dev
