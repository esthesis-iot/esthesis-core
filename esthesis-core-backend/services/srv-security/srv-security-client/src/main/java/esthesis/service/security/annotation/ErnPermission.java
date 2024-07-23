package esthesis.service.security.annotation;

import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for security permissions.
 * This annotation allows you to quickly perform security checks without having to manually call the
 * security service. It should be placed on service methods that require security checks.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ErnPermission {

	// A list of roles for which security evaluation should be bypassed. This is useful for some
	// rare cases, where a method is used both by an authenticated user and an unauthenticated user
	// (mainly, system calls).
	@Nonbinding
	String[] bypassForRoles() default {};

	// The category of the security operation.
	@Nonbinding
	Category category();

	// The operation to be performed.
	@Nonbinding
	Operation operation();

	// The resource ID, if any.
	@Nonbinding
	String resourceId() default "";
}
