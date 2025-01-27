# Redis cache DFL

The Redis Cache DFL is responsible to cache data in Redis and it is a intrinsic part of esthesis CORE. esthesis CORE
uses Redis as a cache to store several different types of data, such as the last measurement received for each sensor,
download tokens for provisioning, ets.

## Inputs

- Redis: The DFL requires the connection details to the Redis database, including the URL, the port, and the password.
- Kafka: The DFL obtains telemetry and metadata from Kafka. It requires Kafka connection details, as well as the topic
  names from which to read metadata and telemetry. You may leave empty either of those, if you do not need to process
  telemetry or metadata respectively.
- Kubernetes: The DFL is deployed as a Kubernetes deployment and requires the Kubernetes connection details.
- Concurrency: You can set the number of messages the DFL can locally queue for processing, the polling frequency, as
  well as the number of parallel execution processing threads.
- Logging: You can specify the logging level for the esthesis components in this DFL, as well as the general logging
  level.

## Outputs

The DFL updates the Redis database with incoming telemetry, metadata, and other values.