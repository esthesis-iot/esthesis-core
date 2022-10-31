#!/usr/bin/env sh
cd dfl-influxdb-writer && ./publish.sh && cd ..
cd dfl-mqtt-client && ./publish.sh && cd ..
cd dfl-ping-updater && ./publish.sh && cd ..
cd dfl-rdbms-writer && ./publish.sh && cd ..
cd dfl-redis-cache && ./publish.sh && cd ..
