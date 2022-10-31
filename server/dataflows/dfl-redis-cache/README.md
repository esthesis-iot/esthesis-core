# Build and run instructions

## Run in dev mode

```
env \
    ESTHESIS_DFL_REDIS_URL=redis://esthesis-dev-redis:6379/0 \
    ESTHESIS_DFL_REDIS_PASSWORD=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_TELEMETRY_TOPIC=esthesis-telemetry \
    ESTHESIS_DFL_KAFKA_METADATA_TOPIC=esthesis-metadata \
    ESTHESIS_DFL_KAFKA_GROUP=dfl-redis-cache \
./mvnw quarkus:dev
```

