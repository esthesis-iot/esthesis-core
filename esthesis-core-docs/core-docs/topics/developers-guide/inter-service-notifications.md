# Inter-service notifications

In a fully distributed microservices system, like esthesis CORE, it is often necessary a service
to be notified of events that occur in other services. For example, when a device is deleted from
the system, it might be necessary to remove the history of commands that were sent to it.

In esthesis CORE, we use a Kafka-based mechanism to notify services of events that occur in other
services. To facilitate and standardise the use of this mechanism, we have created a library named
`util-kafka-notifications`. The aim of this library is not only to provide utility code when you
want to send and receive notifications, but also to provide a standardised way of doing so.

## Standardising components, actions, and messages
The names of the components that can participate in the notification mechanism are defined in
[KafkaNotificationsConstants.java](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/util/utl-kafka-notifications/utl-kafka-notifications-common/src/main/java/esthesis/util/kafka/notifications/common/KafkaNotificationsConstants.java) file. This class is always work in progress, as we keep
adding components and event types to it, so if the component, or action, you are working with is not
included, you can extend it appropriately.

There are three different `enum` classes that you can augment when defining new notification types:
1. `Component`: This class defines the components that can participate in the notification mechanism.
You should add a new entry representing your component.
2. `Subject`: This class defines the subject of the component being targeted by the message. For
example, the Device component may publish messages with different subjects to represent the
different types of the underlying object types it manages. For most components, the Subject name
will be the same as the Component name, especially considering the narrow scope of each
microservice in esthesis CORE.
3. `Action`: This class defines the action that was performed which triggered the message. Try
to reuse the existing actions and only define new action types when absolutely necessary.

For components to be able to understand messages sent by other components, all messages are
published as an [AppMessage](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/util/utl-kafka-notifications/utl-kafka-notifications-common/src/main/java/esthesis/util/kafka/notifications/common/AppMessage.java) class. `AppMessage` encapsulates the information defined in
`KafkaNotificationsConstants` while also providing a custom payload attribute.

## Unicast vs Broadcast notifications
Some notifications are intended to be handled by a single service instance, while others are intended to be
handled by multiple service instances. For example, when a tag is deleted, we want to disassociate this tag from all
devices that use it; in that case, only a single instance of the `Device` service should handle the notification and
proceed removing the tag from the devices. On the other hand, when a user is closing a dashboard we want to unsubscribe
from receiving events for that dashboard; since multiple instances of the `Dashboard` service might be running, and we
do not know which one is handling the SSE broadcaster for that specific client, we want the notification to be broadcasted
to all instances of the `Dashboard` service - the one that is handling the SSE broadcaster for that client will then
unsubscribe the client.

To facilitate this, we have implemented a `unicast` and `broadcast` mechanism in the `util-kafka-notifications` library.
- When sending a notification, you can specify whether the notification should be unicast or broadcast. By default, all
notifications are unicast. To send a broadcast notification, you need to set the `broadcast` attribute of the `AppMessage`
to `true` (or the equivalent annotation, see below).
- When receiving a notification, you just subscribe to the `unicast` or `broadcast` topic accordingly (you can have
your component subscribing to both topics, if necessary). Remember that for the broadcast topic, you need to provide
a random consumer group ID.

## Sending notifications
To facilitate sending notifications from your components we have implemented the [KafkaNotification](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/util/utl-kafka-notifications/utl-kafka-notifications-outgoing/src/main/java/esthesis/util/kafka/notifications/outgoing/KafkaNotification.java)
annotation. The annotation should be used as a method-level annotation, and it will automatically
publish a notification message to Kafka in case your method is executed successfully. The
annotation is processed by [KafkaNotificationInterceptor](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/util/utl-kafka-notifications/utl-kafka-notifications-outgoing/src/main/java/esthesis/util/kafka/notifications/outgoing/KafkaNotificationInterceptor.java).

The annotation has the following attributes:
1. `component`: The component that is sending the notification.
2. `subject`: The subject of the notification.
3. `action`: The action that triggered the notification.
4. `comment`: A comment that can be used to provide more information about the notification.
5. `idParamOrder`: The order of the parameter that contains the ID of the object for the notification.
This parameter starts at 0 to indicate the first parameter of the method. Make sure that if your
method's parameter order changes, you update this attribute accordingly.
6. `idParamRegEx`: A regular expression that is used to extract the ID of the object for the notification.
If used, this attribute takes precedence over `idParamOrder`. This parameter is useful in cases where
the only argument to a method is a  object that contains the ID of the object for the notification.
See how it is used in `register` method in [DeviceRegistrationService.java](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/services/srv-device/srv-device-impl/src/main/java/esthesis/services/device/impl/service/DeviceRegistrationService.java) as an example.
7. `broadcast`: A boolean value that indicates whether the notification should be a broadcast. By default,
notifications are unicast.

### Setup {id="setup-sending"}
To be able to send notification using the above mechanism, you need to set up a few things:

```bash
# dev-{}.sh

KAFKA="true"
```

```yaml
# application.yaml (adapt for unicast and broadcast as needed)

mp:
  messaging:
    outgoing:
      esthesis-app-unicast-out:
        topic: esthesis-app-unicast
      esthesis-app-broadcast-out:
        topic: esthesis-app-broadcast
```

```yaml
# application-dev.yaml

kafka:
  security:
    protocol: SASL_PLAINTEXT
  sasl:
    mechanism: SCRAM-SHA-512
    jaas:
      config: org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;
```

```shell
# Helm service deployment template

"podKafka" true
```

### Example {id="example-sending"}
Let us see how this works taking [DeviceService.java](https://github.com/esthesis-iot/esthesis-platform/blob/7cb8c453e2c507ab8c90b5d47ae56e14b8aa158d/esthesis-core/esthesis-core-backend/services/srv-device/srv-device-impl/src/main/java/esthesis/services/device/impl/service/DeviceService.java#L219) as an example:

```java
@KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE, action = Action.DELETE, idParamOrder = 0)
public boolean deleteById(String deviceId) {
		// ...
}
```

Once `deleteById` is executed successfully, a unicast notification message will be published to Kafka with
the following attributes:
1. `component`: `Component.DEVICE`
2. `subject`: `Subject.DEVICE`
3. `action`: `Action.DELETE`
4. `msgId`: A UUID created automatically.
5. `targetId`: The value of `deviceId`.

## Receiving notifications
To receive the notifications generated with the above mechanism, you can use the built-in Kafka
integration of Quarkus. To keep notification handlers consistent between the different components
of the platform, please create a `notifications` package, with one or more notifications handlers
inside. You can find an example notification handler in [NotificationsHandler.java](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/services/srv-device/srv-device-impl/src/main/java/esthesis/services/device/impl/notifications/NotificationsHandler.java).

### Setup {id="setup-receiving"}
To be able to receive notifications you need to set up a few things:

```bash
# dev-{}.sh

KAFKA="true"
```

```yaml
# application.yaml (adapt for unicast and broadcast as needed)

mp:
  messaging:
    incoming:
      esthesis-app-unicast-in:
        topic: esthesis-app-unicast
      esthesis-app-broadcast-in:
        topic: esthesis-app-broadcast
        group:
          id: ${random.uuid}
```

```yaml
# application-dev.yaml

kafka:
  security:
    protocol: SASL_PLAINTEXT
  sasl:
    mechanism: SCRAM-SHA-512
    jaas:
      config: org.apache.kafka.common.security.scram.ScramLoginModule required username=esthesis-system password=esthesis-system;
```

```shell
# Helm service deployment template

"podKafka" true
```

### Example {id="example-receiving"}
```java
@Blocking
@Incoming(SMALLRYE_KAFKA_BROADCAST_CHANNEL_IN)
private CompletionStage<Void> onMessage(KafkaRecord<String, AppMessage> msg) {
    log.trace("Processing Kafka application message '{}'", msg);

    // Process the message.
    try (Scope scope = msg.getMetadata().get(TracingMetadata.class)
        .map(tm -> tm.getCurrentContext().makeCurrent())
        .orElse(io.opentelemetry.context.Context.current().makeCurrent())) {
        // ...
        }
    } catch (Exception e) {
        log.warn("Could not handle Kafka message '{}'.", msg, e);
    }
```