package esthesis.dataflows.oriongateway.client;

import esthesis.dataflows.oriongateway.service.OrionAuthService;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrionClientHeaderFilterTest {

	@Mock
	ClientRequestContext clientRequestContext;

	@Mock
	OrionAuthService authService;


	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void filter() {
		// Mock the headers map to return a mock object.
		MultivaluedMap<String, Object> mockHearders = Mockito.mock(MultivaluedMap.class);
		Mockito.when(clientRequestContext.getHeaders()).thenReturn(mockHearders);


		// Create an instance of OrionClientHeaderFilter with test data.
		OrionClientHeaderFilter orionClientHeaderFilter = new OrionClientHeaderFilter(List.of("test-context-url"),
			List.of("test-rel"), authService, "test-tenant");

		// Call the filter method.
		orionClientHeaderFilter.filter(clientRequestContext);

		// Verify that the headers were added correctly.
		verify(mockHearders).add("Link", "<test-context-url>; rel=\"test-rel\"");
		verify(mockHearders).add("NGSILD-Tenant", "test-tenant");
		// Verify that the authenticate method of authService was called.
		verify(authService).authenticate(clientRequestContext);
	}
}
