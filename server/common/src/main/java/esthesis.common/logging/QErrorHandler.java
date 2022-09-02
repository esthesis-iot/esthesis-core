package esthesis.common.logging;

import io.opentelemetry.api.trace.Span;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
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
    // Log the error, so the full details are available on the server-side.
    if (uriInfo != null && StringUtils.isNotBlank(uriInfo.getPath())) {
      log.error("'%s' error while processing request to '%s'."
          .formatted(throwable.getMessage(), uriInfo.getPath()), throwable);
    } else {
      log.error("'%s' error.".formatted(throwable.getMessage()), throwable);
    }

    // Prepare a custom response for the client, hiding the underlying error.
    QErrorReply errorReply = new QErrorReply();
    errorReply.setErrorMessage("There was an error processing this request.");
    errorReply.setTraceId(Span.current().getSpanContext().getTraceId());

    return Response.serverError().entity(errorReply).build();
  }
}
