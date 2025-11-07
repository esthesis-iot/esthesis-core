package esthesis.service.common.logging;

import esthesis.common.exception.QSecurityException;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for QErrorHandler, testing error handling functionality.
 */
class QErrorHandlerTest {

	@InjectMocks
	QErrorHandler qErrorHandler;

	@Mock
	UriInfo uriInfo;


	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		Mockito.when(uriInfo.getPath()).thenReturn("/test");
	}

	@Test
	void toResponse() {
		assertNotNull(qErrorHandler);
		assertEquals(Status.UNAUTHORIZED.getStatusCode(),
			qErrorHandler.toResponse(new QSecurityException("Unauthorized")).getStatus());

		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(),
			qErrorHandler.toResponse(new RuntimeException("Unexpected error")).getStatus());


	}
}
