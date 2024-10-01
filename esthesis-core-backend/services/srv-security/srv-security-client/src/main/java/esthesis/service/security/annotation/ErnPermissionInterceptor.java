package esthesis.service.security.annotation;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.core.common.entity.BaseEntity;
import esthesis.common.exception.QSecurityException;
import esthesis.service.security.resource.SecurityResource;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@ErnPermission(category = Category.NULL, operation = Operation.NULL)
public class ErnPermissionInterceptor {

	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	@RestClient
	SecurityResource securityResource;

	@AroundInvoke
	Object checkPermission(InvocationContext ctx) throws Exception {
		log.trace("Checking permission for method '{}' on class '{}.",
			ctx.getMethod().getName(), ctx.getTarget().getClass().getName());
		// Get annotation parameters.
		ErnPermission ernPermission = ctx.getMethod().getAnnotation(ErnPermission.class);
		log.trace("Security annotation parameter: '{}'", ernPermission);

		if (Arrays.stream(ernPermission.bypassForRoles()).noneMatch(securityIdentity::hasRole)) {
			log.trace("Security check will be performed.");

			// Get entity id.
			String resourceId = null;
			if (ernPermission.checkResourceId() && ctx.getParameters() != null &&
				ctx.getParameters().length > 0 && ctx.getParameters()[0] != null) {
				Object arg0 = ctx.getParameters()[0];
				if (arg0 instanceof String entityId && !entityId.equals(AppConstants.NEW_RECORD_ID)) {
					resourceId = entityId;
				} else if (arg0 instanceof BaseEntity baseEntity && baseEntity.getId() != null) {
					resourceId = baseEntity.getId().toHexString();
				}
				log.trace("Found resource ID: '{}'.", resourceId);
			}

			// Perform security check.
			if (resourceId != null) {
				if (!securityResource.isPermitted(ernPermission.category(), ernPermission.operation(),
					resourceId)) {
					throw new QSecurityException("You are not allowed to perform this operation.");
				}
			} else {
				if (!securityResource.isPermitted(ernPermission.category(), ernPermission.operation())) {
					throw new QSecurityException("You are not allowed to perform this operation.");
				}
			}
		} else {
			log.trace("Security check will be bypassed.");
		}

		// Proceed with the invocation.
		return ctx.proceed();
	}
}
