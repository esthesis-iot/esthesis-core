package esthesis.util.kafka.notifications.outgoing;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.SMALLRYE_KAFKA_CHANNEL_OUT;

import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.AppMessage.AppMessageBuilder;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import io.opentelemetry.context.Context;
import io.quarkus.arc.Priority;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@KafkaNotification(component = Component.UNSPECIFIED, subject = Subject.UNSPECIFIED, action = Action.UNSPECIFIED)
public class KafkaNotificationInterceptor {

  @Inject
  @Channel(SMALLRYE_KAFKA_CHANNEL_OUT)
  Emitter<AppMessage> emitter;

  @AroundInvoke
  Object notify(InvocationContext ctx) throws Exception {
    // Get a reference to the annotation and find the id of the message.
    Method method = ctx.getMethod();
    KafkaNotification kafkaNotification = method.getAnnotation(KafkaNotification.class);
    String id = null;
    if (kafkaNotification.idParamOrder() > -1) {
      id = (String) ctx.getParameters()[kafkaNotification.idParamOrder()];
    }

    // Construct the message to emit.
    AppMessageBuilder msgBuilder = AppMessage.builder().component(kafkaNotification.component())
        .subject(kafkaNotification.subject()).action(kafkaNotification.action());
    if (StringUtils.isNotBlank(id)) {
      msgBuilder.id(id);
    }
    if (StringUtils.isNotBlank(kafkaNotification.payload())) {
      msgBuilder.payload(kafkaNotification.payload());
    }
    Message<AppMessage> msg = Message.of(msgBuilder.build())
        .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
            .withKey(kafkaNotification.subject().toString()).build())
        .addMetadata(TracingMetadata.withCurrent(Context.current()));
    log.debug("Sending Kafka notification '{}'.", msg.getPayload());

    Object proceed = ctx.proceed();
    emitter.send(msg);

    return proceed;
  }
}
