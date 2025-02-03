package esthesis.service.audit.ccc;

import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Audited {

	enum AuditLogType {
		// An indicator that incoming date (i.e. parameters) should be logged.
		DATA_IN,
		// An indicator that outgoing data (i.e. return values) should be logged.
		DATA_OUT,
		// An indicator that both incoming and outgoing data should be logged.
		DATA_ALL
	}

	// The operation type being audited.
	@Nonbinding
	Operation op();

	// The category of the operation being audited.
	@Nonbinding
	Category cat();

	// The message to be logged.
	@Nonbinding
	String msg();

	// The type of data to be logged, see {@link AuditLogType}.
	@Nonbinding
	AuditLogType log() default AuditLogType.DATA_ALL;
}
