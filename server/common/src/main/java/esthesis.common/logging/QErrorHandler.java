package esthesis.common.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Provider
@Priority(10000)
public class QErrorHandler implements ExceptionMapper<Exception> {

  @Inject
  ObjectMapper mapper;

  @Inject
  UriInfo uriInfo;

  @Override
  @SneakyThrows
  public Response toResponse(Exception exception) {
    return mapExceptionToResponse(exception);
  }

  private Response mapExceptionToResponse(Exception exception)
  throws JsonProcessingException {
    if (exception instanceof javax.ws.rs.WebApplicationException) {
      Response originalErrorResponse = ((WebApplicationException) exception).getResponse();

      return Response.fromResponse(originalErrorResponse)
          .entity(originalErrorResponse.getStatusInfo().getReasonPhrase())
          .build();
    } else {
      if (uriInfo != null && StringUtils.isNotBlank(uriInfo.getPath())) {
        log.error("Failed to process request to: " + uriInfo.getPath(),
            exception);
      } else {
        log.error(exception.getMessage(), exception);
      }

      QErrorReply errorReply = new QErrorReply();
      errorReply.setErrorMessage(exception.getMessage());
      errorReply.setTraceId(Span.current().getSpanContext().getTraceId());

      return Response.serverError()
          .entity(mapper.writeValueAsString(errorReply)).build();
    }
  }
}
