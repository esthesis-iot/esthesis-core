# Build and run instructions

## Run in dev mode

```
env \
    ESTHESIS_DFL_INFLUX_URL=http://esthesis-dev-influxdb:8086 \
    ESTHESIS_DFL_INFLUX_TOKEN=Orlx1ELFS6_cBucJKVq4reftpB9_maMJMGKSAKOAIj2sVkF8ysowQLGUMhzj7yuhTuhMqhBzPXmU1xh9o60dKA== \
    ESTHESIS_DFL_INFLUX_ORG=esthesis \
    ESTHESIS_DFL_INFLUX_BUCKET=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_GROUP=dfl-influxdb-writer \
./mvnw quarkus:dev
```

```
docker run \
    --env ESTHESIS_DFL_INFLUX_URL=http://esthesis-dev-influxdb:8086 \
    --env ESTHESIS_DFL_INFLUX_TOKEN=Orlx1ELFS6_cBucJKVq4reftpB9_maMJMGKSAKOAIj2sVkF8ysowQLGUMhzj7yuhTuhMqhBzPXmU1xh9o60dKA== \
    --env ESTHESIS_DFL_INFLUX_ORG=esthesis \
    --env ESTHESIS_DFL_INFLUX_BUCKET=esthesis \
    --env ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    --env ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    --env ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    --env ESTHESIS_DFL_KAFKA_GROUP=dfl-influxdb-writer \
    --rm -ti \
esthesisiot/esthesis-dfl-influxdb-writer:latest
```
