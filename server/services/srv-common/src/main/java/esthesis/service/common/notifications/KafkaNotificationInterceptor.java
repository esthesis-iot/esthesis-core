package esthesis.service.common.notifications;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.MessagingKafka;
import esthesis.common.AppConstants.MessagingKafka.Action;
import esthesis.common.AppConstants.MessagingKafka.Component;
import esthesis.common.AppConstants.MessagingKafka.Subject;
import esthesis.common.kafka.AppMessage;
import esthesis.common.kafka.AppMessage.AppMessageBuilder;
import io.micrometer.common.util.StringUtils;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import java.lang.reflect.Method;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@Interceptor
@KafkaNotification(component = Component.UNSPECIFIED, subject = Subject.UNSPECIFIED,
    action = Action.UNSPECIFIED)
public class KafkaNotificationInterceptor {

  @Inject
  @Channel(AppConstants.MessagingKafka.KAFKA_TOPIC)
  Emitter<AppMessage> emitter;

  @AroundInvoke
  Object audit(InvocationContext ctx) throws Exception {
    // Get a reference to the annotation and find the id of the message.
    Method method = ctx.getMethod();
    KafkaNotification kafkaNotification = method.getAnnotation(KafkaNotification.class);
    String id = null;
    if (kafkaNotification.idParamOrder() > -1) {
      id = (String) ctx.getParameters()[kafkaNotification.idParamOrder()];
    }

    // Construct the message to emit.
    AppMessageBuilder msgBuilder =
        AppMessage.builder().component(kafkaNotification.component())
            .subject(kafkaNotification.subject())
            .action(kafkaNotification.action());
    if (StringUtils.isNotBlank(id)) {
      msgBuilder.id(id);
    }
    if (StringUtils.isNotBlank(kafkaNotification.payload())) {
      msgBuilder.payload(kafkaNotification.payload());
    }
    Message<AppMessage> msg = Message.of(msgBuilder.build())
        .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
            .withTopic(MessagingKafka.KAFKA_TOPIC)
            .withKey(kafkaNotification.subject().toString())
            .build())
        .addMetadata(TracingMetadata.withCurrent(Context.current()));
    log.trace("Sending Kafka notification '{}'", msg.getPayload());

    emitter.send(msg);

    return ctx.proceed();
  }
}
