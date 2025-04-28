package esthesis.service.audit.ccc;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.core.common.AppConstants;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditResource;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.interceptor.InvocationContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AuditedInterceptorTest {


	@InjectMocks
	AuditedInterceptor auditedInterceptor;

	@Mock
	SecurityIdentity securityIdentity;

	@Mock
	AuditResource auditResource;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		auditedInterceptor = new AuditedInterceptor();
		auditedInterceptor.securityIdentity = securityIdentity;
		auditedInterceptor.auditResource = auditResource;
		auditedInterceptor.mapper = new ObjectMapper();
	}

	@SneakyThrows
	@Test
	void audit() {

		when(securityIdentity.getPrincipal()).thenReturn(getPrincipal("test-user"));
		when(auditResource.save(any())).thenReturn(new AuditEntity());

		InvocationContext ctx = mock(InvocationContext.class);
		Method method = mock(Method.class);
		Audited audited = mock(Audited.class);

		when(ctx.getMethod()).thenReturn(method);
		when(method.getAnnotation(Audited.class)).thenReturn(audited);
		when(ctx.getParameters()).thenReturn(new String[]{"param1", "param2"});
		when(ctx.proceed()).thenReturn("{\"result\": \"success\"}");

		when(securityIdentity.getPrincipal()).thenReturn(getPrincipal("test-user"));
		when(audited.cat()).thenReturn(AppConstants.Security.Category.ABOUT);
		when(audited.op()).thenReturn(AppConstants.Security.Operation.AUDIT);
		when(audited.msg()).thenReturn("test message");
		when(audited.log()).thenReturn(Audited.AuditLogType.DATA_ALL);


		assertDoesNotThrow(() -> auditedInterceptor.audit(ctx));

		verify(auditResource).save(any());

	}


	private Principal getPrincipal(String username) {
		return () -> username;
	}
}
