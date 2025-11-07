package esthesis.service.security.annotation;

import esthesis.common.exception.QSecurityException;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.security.resource.SecurityResource;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.interceptor.InvocationContext;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for ErnPermissionInterceptor, testing permission interceptor functionality.
 */

class ErnPermissionInterceptorTest {

	ErnPermissionInterceptor interceptor;
	SecurityIdentity identity;
	SecurityResource securityResource;
	InvocationContext ctx;

	@BeforeEach
	void setup() throws Exception {
		interceptor = new ErnPermissionInterceptor();
		identity = mock(SecurityIdentity.class);
		securityResource = mock(SecurityResource.class);
		ctx = mock(InvocationContext.class);

		interceptor.securityIdentity = identity;
		interceptor.securityResource = securityResource;

		Method dummyMethod = DummyClass.class.getMethod("secureMethod", Object.class);
		when(ctx.getMethod()).thenReturn(dummyMethod);
		when(ctx.getTarget()).thenReturn(new DummyClass());
		when(ctx.proceed()).thenReturn("executed");
	}

	@Test
	void testBypassForRole() throws Exception {
		when(identity.hasRole("admin")).thenReturn(true);
		Object result = interceptor.checkPermission(ctx);
		assertEquals("executed", result);
	}

	@Test
	void testPermittedWithoutResourceId() throws Exception {
		when(identity.hasRole("admin")).thenReturn(false);
		when(ctx.getParameters()).thenReturn(new Object[]{});
		when(securityResource.isPermitted(Category.DATAFLOW, Operation.READ)).thenReturn(true);

		Object result = interceptor.checkPermission(ctx);
		assertEquals("executed", result);
	}

	@Test
	void testPermittedWithResourceIdAsString() throws Exception {
		when(identity.hasRole("admin")).thenReturn(false);
		String resourceId = new ObjectId().toHexString();
		when(ctx.getParameters()).thenReturn(new Object[]{resourceId});
		when(securityResource.isPermitted(Category.DATAFLOW, Operation.READ, resourceId)).thenReturn(true);

		Object result = interceptor.checkPermission(ctx);
		assertEquals("executed", result);
	}

	@Test
	void testPermittedWithResourceIdFromEntity() throws Exception {
		when(identity.hasRole("admin")).thenReturn(false);
		BaseEntity entity = mock(BaseEntity.class);
		ObjectId objectId = new ObjectId();
		when(entity.getId()).thenReturn(objectId);
		when(ctx.getParameters()).thenReturn(new Object[]{entity});
		when(securityResource.isPermitted(Category.DATAFLOW, Operation.READ, objectId.toHexString()))
			.thenReturn(true);

		Object result = interceptor.checkPermission(ctx);
		assertEquals("executed", result);
	}

	@Test
	void testNotPermittedThrowsException(){
		when(identity.hasRole("admin")).thenReturn(false);
		when(ctx.getParameters()).thenReturn(new Object[]{});
		when(securityResource.isPermitted(Category.DATAFLOW, Operation.READ)).thenReturn(false);

		assertThrows(QSecurityException.class, () -> interceptor.checkPermission(ctx));
	}

	public static class DummyClass {
		@ErnPermission(category = Category.DATAFLOW, operation = Operation.READ, bypassForRoles = {"admin"}, checkResourceId = true)
		public String secureMethod(Object param) {
			return "executed";
		}
	}
}
