package esthesis.service.security.annotation;

import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
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

	// Specifies whether the security check should include a check for the resource id being
	// accessed. If this is set to 'true', it is expected that the resource id can be found as the
	// first parameter of the method being annotated. The resource id can be either a string or a
	// BaseEntity object. If the resource id can not be successfully extracted, a warning will be
	// logged and the security check will be performed without including the resource id.
	@Nonbinding
	boolean checkResourceId() default true;
}
