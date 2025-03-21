package esthesis.util.kafka.notifications.outgoing;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.SMALLRYE_KAFKA_BROADCAST_CHANNEL_OUT;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.SMALLRYE_KAFKA_UNICAST_CHANNEL_OUT;

import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.AppMessage.AppMessageBuilder;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import io.opentelemetry.context.Context;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

/**
 * Interceptor for Kafka notifications.
 */
@Slf4j
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@KafkaNotification(component = Component.UNSPECIFIED, subject = Subject.UNSPECIFIED, action = Action.UNSPECIFIED)
public class KafkaNotificationInterceptor {

	@Inject
	@Channel(SMALLRYE_KAFKA_UNICAST_CHANNEL_OUT)
	Emitter<AppMessage> emitterUnicast;

	@Inject
	@Channel(SMALLRYE_KAFKA_BROADCAST_CHANNEL_OUT)
	Emitter<AppMessage> emitterBroadcast;

	@Inject
	SecurityIdentity securityIdentity;

	@AroundInvoke
	Object notify(InvocationContext ctx) throws Exception {
		// Proceed with the invocation.
		Object proceed = ctx.proceed();

		// Get a reference to the annotation to inspect parameters.
		Method method = ctx.getMethod();
		KafkaNotification kafkaNotification = method.getAnnotation(KafkaNotification.class);
		String targetId = null;

		// Extract the target ID from the method parameters.
		if (StringUtils.isNotEmpty(kafkaNotification.idParamRegEx())) {
			Pattern pattern = Pattern.compile(kafkaNotification.idParamRegEx(), Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(proceed.toString());
			if (matcher.find()) {
				targetId = matcher.group(1);
			}
		} else if (kafkaNotification.idParamOrder() > -1) {
			targetId = (String) ctx.getParameters()[kafkaNotification.idParamOrder()];
		}

		// Construct the message to emit.
		AppMessageBuilder msgBuilder = AppMessage.builder()
			.component(kafkaNotification.component())
			.subject(kafkaNotification.subject())
			.action(kafkaNotification.action())
			.broadcast(kafkaNotification.broadcast())
			.msgId(UUID.randomUUID().toString());
		if (StringUtils.isNotBlank(targetId)) {
			msgBuilder.targetId(targetId);
		}
		if (StringUtils.isNotBlank(kafkaNotification.comment())) {
			msgBuilder.comment(kafkaNotification.comment());
		}
		// Set user id and trace context.
		String username;
		if (securityIdentity != null && securityIdentity.getPrincipal() != null &&
			StringUtils.isNotEmpty(securityIdentity.getPrincipal().getName())) {
			username = securityIdentity.getPrincipal().getName();
		} else {
			username = "SYSTEM";
		}
		Message<AppMessage> msg = Message.of(msgBuilder.build())
			.addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
				.withKey(username).build())
			.addMetadata(TracingMetadata.withCurrent(Context.current()));
		log.trace("Sending Kafka notification '{}'.", msg.getPayload());

		// Emit the message.
		if (kafkaNotification.broadcast()) {
			emitterBroadcast.send(msg);
		} else {
			emitterUnicast.send(msg);
		}

		return proceed;
	}
}
