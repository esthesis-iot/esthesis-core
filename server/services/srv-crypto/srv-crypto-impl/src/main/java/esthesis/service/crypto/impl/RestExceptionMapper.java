package esthesis.service.crypto.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@Provider
public class RestExceptionMapper implements
    ExceptionMapper<ClientWebApplicationException> {

  @Override
  public Response toResponse(ClientWebApplicationException exception) {
//    System.out.println(">>>>>>>>>>>>>>>>>>> " + exception);
//    return Response.status(exception.getResponse().getStatus()).build();
    throw exception;
  }
}
