package esthesis.service.common.logging;

import esthesis.common.exception.QSecurityException;
import io.opentelemetry.api.trace.Span;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Provider
@Priority(Integer.MAX_VALUE)
public class QErrorHandler implements ExceptionMapper<Throwable> {

	@Inject
	UriInfo uriInfo;

	@Override
	@SneakyThrows
	public Response toResponse(Throwable throwable) {
		return mapExceptionToResponse(throwable);
	}

	private Response mapExceptionToResponse(Throwable throwable) {
		String errorMessage = "";
		if (throwable.getMessage() != null) {
			errorMessage = throwable.getMessage();
		}

		// Log the error, so the full details are available on the server-side.
		if (uriInfo != null && StringUtils.isNotBlank(uriInfo.getPath())) {
			log.error(
				"Error '%s' while processing request to '%s'.".formatted(errorMessage,
					uriInfo.getPath()), throwable);
		} else {
			log.error("Error '%s'.".formatted(errorMessage), throwable);
		}

		// Prepare a custom response for the client, hiding the underlying error details.
		QErrorReply errorReply = new QErrorReply();
		errorReply.setErrorMessage(errorMessage);
		errorReply.setTraceId(Span.current().getSpanContext().getTraceId());

		// Handle response status code.
		if (throwable instanceof QSecurityException) {
			return Response.status(Status.UNAUTHORIZED).entity(errorReply).build();
		} else {
			return Response.serverError().entity(errorReply).build();
		}
	}
}
