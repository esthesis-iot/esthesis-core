package esthesis.service.security.annotation;

import esthesis.common.exception.QSecurityException;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.security.resource.SecurityResource;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("WIP")
class ErnPermissionInterceptorTest {

	@Mock
	private SecurityIdentity securityIdentity;

	@Mock
	private SecurityResource securityResource;

	@Mock
	private InvocationContext invocationContext;

	@InjectMocks
	private ErnPermissionInterceptor ernPermissionInterceptor;

	@Test
	void allowsInvocationWhenBypassRoleIsPresent() throws Exception {
		ErnPermission ernPermission = mock(ErnPermission.class);
		when(ernPermission.bypassForRoles()).thenReturn(new String[]{"ADMIN"});
		when(invocationContext.getMethod()).thenReturn(this.getClass().getMethod("allowsInvocationWhenBypassRoleIsPresent"));
		when(invocationContext.getMethod().getAnnotation(ErnPermission.class)).thenReturn(ernPermission);
		when(securityIdentity.hasRole("ADMIN")).thenReturn(true);

		ernPermissionInterceptor.checkPermission(invocationContext);

		verify(invocationContext).proceed();
	}

	@Test
	void deniesInvocationWhenPermissionIsNotGranted() throws Exception {
		ErnPermission ernPermission = mock(ErnPermission.class);
		when(ernPermission.bypassForRoles()).thenReturn(new String[]{});
		when(ernPermission.category()).thenReturn(Category.USERS);
		when(ernPermission.operation()).thenReturn(Operation.READ);
		when(invocationContext.getMethod()).thenReturn(this.getClass().getMethod("deniesInvocationWhenPermissionIsNotGranted"));
		when(invocationContext.getMethod().getAnnotation(ErnPermission.class)).thenReturn(ernPermission);
		when(securityResource.isPermitted(Category.USERS, Operation.READ)).thenReturn(false);

		assertThrows(QSecurityException.class, () -> ernPermissionInterceptor.checkPermission(invocationContext));
	}

	@Test
	void allowsInvocationWhenPermissionIsGranted() throws Exception {
		ErnPermission ernPermission = mock(ErnPermission.class);
		when(ernPermission.bypassForRoles()).thenReturn(new String[]{});
		when(ernPermission.category()).thenReturn(Category.USERS);
		when(ernPermission.operation()).thenReturn(Operation.READ);
		when(invocationContext.getMethod()).thenReturn(this.getClass().getMethod("allowsInvocationWhenPermissionIsGranted"));
		when(invocationContext.getMethod().getAnnotation(ErnPermission.class)).thenReturn(ernPermission);
		when(securityResource.isPermitted(Category.USERS, Operation.READ)).thenReturn(true);

		ernPermissionInterceptor.checkPermission(invocationContext);

		verify(invocationContext).proceed();
	}

	@Test
	void deniesInvocationWhenResourceIdPermissionIsNotGranted() throws Exception {
		ErnPermission ernPermission = mock(ErnPermission.class);
		when(ernPermission.bypassForRoles()).thenReturn(new String[]{});
		when(ernPermission.category()).thenReturn(Category.USERS);
		when(ernPermission.operation()).thenReturn(Operation.READ);
		when(ernPermission.checkResourceId()).thenReturn(true);
		when(invocationContext.getMethod()).thenReturn(this.getClass().getMethod("deniesInvocationWhenResourceIdPermissionIsNotGranted"));
		when(invocationContext.getMethod().getAnnotation(ErnPermission.class)).thenReturn(ernPermission);
		when(invocationContext.getParameters()).thenReturn(new Object[]{"resource-id"});
		when(securityResource.isPermitted(Category.USERS, Operation.READ, "resource-id")).thenReturn(false);

		assertThrows(QSecurityException.class, () -> ernPermissionInterceptor.checkPermission(invocationContext));
	}

	@Test
	void allowsInvocationWhenResourceIdPermissionIsGranted() throws Exception {
		ErnPermission ernPermission = mock(ErnPermission.class);
		when(ernPermission.bypassForRoles()).thenReturn(new String[]{});
		when(ernPermission.category()).thenReturn(Category.USERS);
		when(ernPermission.operation()).thenReturn(Operation.READ);
		when(ernPermission.checkResourceId()).thenReturn(true);
		when(invocationContext.getMethod()).thenReturn(this.getClass().getMethod("allowsInvocationWhenResourceIdPermissionIsGranted"));
		when(invocationContext.getMethod().getAnnotation(ErnPermission.class)).thenReturn(ernPermission);
		when(invocationContext.getParameters()).thenReturn(new Object[]{"resource-id"});
		when(securityResource.isPermitted(Category.USERS, Operation.READ, "resource-id")).thenReturn(true);

		ernPermissionInterceptor.checkPermission(invocationContext);

		verify(invocationContext).proceed();
	}
}
