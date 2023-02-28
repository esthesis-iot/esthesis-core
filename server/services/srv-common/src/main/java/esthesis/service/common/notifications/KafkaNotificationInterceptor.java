package esthesis.service.common.notifications;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.MessagingKafka.Action;
import esthesis.common.AppConstants.MessagingKafka.Component;
import esthesis.common.AppConstants.MessagingKafka.Subject;
import esthesis.common.kafka.AppMessage;
import esthesis.common.kafka.AppMessage.AppMessageBuilder;
import io.opentelemetry.context.Context;
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
@KafkaNotification(component = Component.UNSPECIFIED, subject = Subject.UNSPECIFIED, action = Action.UNSPECIFIED)
public class KafkaNotificationInterceptor {

  @Inject
  @Channel(AppConstants.MessagingKafka.SMALLRYE_KAFKA_CHANNEL + "-out")
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
//            .withTopic(MessagingKafka.SMALLRYE_KAFKA_CHANNEL)
            .withKey(kafkaNotification.subject().toString()).build())
        .addMetadata(TracingMetadata.withCurrent(Context.current()));
    log.debug("Sending Kafka notification '{}'.", msg.getPayload());

    Object proceed = ctx.proceed();
    emitter.send(msg);

    return proceed;
  }
}
