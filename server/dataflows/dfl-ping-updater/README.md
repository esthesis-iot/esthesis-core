# Build and run instructions

## Run in dev mode

```
env \
    ESTHESIS_DFL_ESTHESIS_DB_URL=mongodb://esthesis-dev-mongodb:27017 \
    ESTHESIS_DFL_ESTHESIS_DB_NAME=esthesis \
    ESTHESIS_DFL_ESTHESIS_DB_USERNAME=esthesis \
    ESTHESIS_DFL_ESTHESIS_DB_PASSWORD=esthesis \
    ESTHESIS_DFL_KAFKA_CLUSTER_URL=esthesis-dev-kafka:9094 \
    ESTHESIS_DFL_KAFKA_PING_TOPIC=esthesis-ping \
./mvnw quarkus:dev
```

## Sample data

```shell
mosquitto_pub -h esthesis-dev-rabbitmq -t esthesis/ping/abc123 -m "health ping=2022-10-25T14:28:25Z"
```
