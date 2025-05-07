package esthesis.service.dt.impl.security;

import esthesis.service.application.resource.ApplicationSystemResource;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DTSecurityFilterProviderTest {

	@Mock
	private ApplicationSystemResource applicationSystemResource;

	@Mock
	private ContainerRequestContext requestContext;

	@InjectMocks
	private DTSecurityFilterProvider dtSecurityFilterProvider;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void filter_AllowsRequestWhenTokenIsValid() {
		String validToken = "valid-token";
		when(requestContext.getHeaderString("X-ESTHESIS-DT-APP")).thenReturn(validToken);
		when(applicationSystemResource.isTokenValid(validToken)).thenReturn(true);

		dtSecurityFilterProvider.filter(requestContext);

		verify(requestContext, never()).abortWith(any());
	}

	@Test
	void filter_AbortsRequestWhenTokenIsInvalid() {
		String invalidToken = "invalid-token";
		when(requestContext.getHeaderString("X-ESTHESIS-DT-APP")).thenReturn(invalidToken);
		when(applicationSystemResource.isTokenValid(invalidToken)).thenReturn(false);

		dtSecurityFilterProvider.filter(requestContext);

		verify(requestContext).abortWith(argThat(response -> response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	void filter_AbortsRequestWhenTokenIsMissing() {
		when(requestContext.getHeaderString("X-ESTHESIS-DT-APP")).thenReturn(null);

		dtSecurityFilterProvider.filter(requestContext);

		verify(requestContext).abortWith(argThat(response -> response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	void filter_AbortsRequestWhenTokenIsEmpty() {
		when(requestContext.getHeaderString("X-ESTHESIS-DT-APP")).thenReturn("");

		dtSecurityFilterProvider.filter(requestContext);

		verify(requestContext).abortWith(argThat(response -> response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()));
	}
}
