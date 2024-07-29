# Inter-service notifications

In a fully distributed microservices system, like esthesis CORE, it is often neccessary a service
to be notified of events that occur in other services. For example, when a device is deleted from
the system, it might be necessary to remove the history of commands that were sent to it.

In esthesis CORE, we use a Kafka-based mechanism to notify services of events that occur in other
services. To facilitate and standardise the use of this mechanism, we have created a library named
`utl-kafka-notifications`. The aim of this library is not only to provide utility code when you
want to send and receive notifications, but also to provide a standardised way of doing so.

## Standardising components and actions
The names of the components that can participate in the notification mechanism are defined in
[KafkaNotificationsConstants.java](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/util/utl-kafka-notifications/utl-kafka-notifications-common/src/main/java/esthesis/util/kafka/notifications/common/KafkaNotificationsConstants.java) file. This class is always work in progress, as we keep
adding components and event types to it, so if the component, or action, you are working with is not
included, you can extend it appropriately.

There are three different `enum` classes that you can extend:
1. `Component`: This class defines the components that can participate in the notification mechanism.
You should add a new entry representing your component.
2. `Subject`: This class defines the subject of the component being targeted by the message. For
example, the Device component may publish messages with different subjects to represent the
different types of the underlying object types it manages. For most components, the Subject name
will be the same as the Component name, especially considering the narrow scope of each
microservice in esthesis CORE.
3. `Action`: This class defines the action that was performed which triggered the message. Try
to reuse the existing actions and only define new action types when absolutely necessary.

## The standard message
For components to be able to understand messages sent by other components, all messages are
published as an [AppMessage](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/util/utl-kafka-notifications/utl-kafka-notifications-common/src/main/java/esthesis/util/kafka/notifications/common/AppMessage.java) class. `AppMessage` encapsulates the information defined in
`KafkaNotificationsConstants` while also providing a custom payload attribute.

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
the only argument to a method is a complex object that contains the ID of the object for the notification.
See how it is used in `register` method in [DeviceRegistrationService.java](https://github.com/esthesis-iot/esthesis-platform/blob/main/esthesis-core/esthesis-core-backend/services/srv-device/srv-device-impl/src/main/java/esthesis/services/device/impl/service/DeviceRegistrationService.java) as an example.

### Setup
To be able to send notification using the above mechanism, you need to set up in your component's
applications YAML the following:
- The `kafka.bootstrap.servers` property, to connect your component to the Kafka broker.
- The `mp.messaging.outgoing.esthesis-app-out.topic` property set to `esthesis-app`.

### Example
Let us see how this works taking [DeviceService.java](https://github.com/esthesis-iot/esthesis-platform/blob/7cb8c453e2c507ab8c90b5d47ae56e14b8aa158d/esthesis-core/esthesis-core-backend/services/srv-device/srv-device-impl/src/main/java/esthesis/services/device/impl/service/DeviceService.java#L219) as an example:

```
@KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE,
	action = Action.DELETE, idParamOrder = 0, payload = "Device ID")
public boolean deleteById(String deviceId) {
		// ...
}
```

Once `deleteById` is executed successfully, a notification message will be published to Kafka with
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

### Setup
To be able to receive notifications using the above mechanism, you need to set up in your component's
applications YAML the following:
- The `kafka.bootstrap.servers` property, to connect your component to the Kafka broker.
- The `mp.messaging.incoming.esthesis-app-in.topic` property set to `esthesis-app`.
