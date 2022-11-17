#!/usr/bin/env bash

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

./mvnw quarkus:dev \
  -Dquarkus.http.port=59030 \
  -Ddebug="$(randomPort)" \
  -Dredis.url=redis://esthesis-dev-redis:6379/0 \
  -Dquarkus.profile=dev
