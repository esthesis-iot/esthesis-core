package esthesis.service.security.annotation;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@ErnPermission
public class ErnPermissionInterceptor {

	@AroundInvoke
	Object checkPermission(InvocationContext ctx) throws Exception {
		log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> BEFORE");
		// Proceed with the invocation.
		Object proceed = ctx.proceed();
		log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> AFTER");

		return proceed;
	}
}
