package esthesis.dataflow.common;

import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QExceptionWrapper;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for RestClientExceptionMapper, testing REST client exception mapping functionality.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("ThrowableNotThrown")
class RestClientExceptionMapperTest {
	private RestClientExceptionMapper exceptionMapper;

	@BeforeEach
	void setUp() {
		exceptionMapper = new RestClientExceptionMapper();
	}

	@Test
	void testToThrowable_ThrowsQDoesNotExistException() {
		try (Response response = mock(Response.class)) {
			when(response.getStatus()).thenReturn(404);
			when(response.getStatusInfo()).thenReturn(Response.Status.NOT_FOUND);

			assertThrows(QDoesNotExistException.class, () ->
				exceptionMapper.toThrowable(response)
			);
		}
	}


	@Test
	void testToThrowable_ThrowsQExceptionWrapper() {
		try (Response response = mock(Response.class)) {
			when(response.getStatus()).thenReturn(500);
			when(response.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);

			assertThrows(QExceptionWrapper.class, () ->
				exceptionMapper.toThrowable(response)
			);
		}

	}
}
