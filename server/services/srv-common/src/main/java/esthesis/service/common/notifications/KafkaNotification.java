package esthesis.service.common.notifications;

import esthesis.common.AppConstants.MessagingKafka.Action;
import esthesis.common.AppConstants.MessagingKafka.Component;
import esthesis.common.AppConstants.MessagingKafka.Subject;
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

  // The order of the parameter that contains the id of the message. If this is not set by the
  // user of this annotation, it takes the default value "-1" which effectively omits the id from
  // the generated message.
  @Nonbinding
  int idParamOrder() default -1;

  @Nonbinding
  Component component();

  @Nonbinding
  Subject subject();

  @Nonbinding
  Action action();

  @Nonbinding
  String payload() default "";

}
