package esthesis.service.audit.ccc;

import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Audited {

  @Nonbinding
  Operation op();

  @Nonbinding
  Category cat();

  @Nonbinding
  String msg();

  @Nonbinding
  AuditLogType log() default AuditLogType.DATA_ALL;

  enum AuditLogType {
    DATA_IN,
    DATA_OUT,
    DATA_ALL
  }
}
