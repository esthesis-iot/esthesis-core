package esthesis.util.kafka.notifications.outgoing;

import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation allowing methods to emit messages to Kafka.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface KafkaNotification {

	// The component this notification is generated for.
	@Nonbinding
	Component component();

	// The subject of the component.
	@Nonbinding
	Subject subject();

	// The action that took place.
	@Nonbinding
	Action action();

	// A comment to be included with the message.
	@Nonbinding
	String comment() default "";

	// The order of the incoming parameter that contains the id value of the generated Kafka message.
	// If this is parameter is not set, it takes the default value "-1" which effectively omits the
	// id from the generated Kafka message.
	@Nonbinding
	int idParamOrder() default -1;

	// A regular expression to extract the id value of the generated Kafka message from the return
	// payload of the annotated method. If this parameter is set, idParamOrder is ignored. This
	// parameter should include a single capturing group.
	@Nonbinding
	String idParamRegEx() default "";
}
