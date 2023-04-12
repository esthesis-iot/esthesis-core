package esthesis.util.kafka.notifications.outgoing;

import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * An annotation to allow methods to emit messages to Kafka.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface KafkaNotification {

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

  @Nonbinding
  Component component();

  @Nonbinding
  Subject subject();

  @Nonbinding
  Action action();

  @Nonbinding
  String payload() default "";

}
