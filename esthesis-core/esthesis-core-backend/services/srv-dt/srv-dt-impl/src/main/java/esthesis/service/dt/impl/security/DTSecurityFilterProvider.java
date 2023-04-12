package esthesis.service.dt.impl.security;

import esthesis.service.application.resource.ApplicationSystemResource;
import esthesis.service.dt.security.DTSecurityFilter;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Provider
@DTSecurityFilter
@ApplicationScoped
public class DTSecurityFilterProvider implements ContainerRequestFilter {

  private static final String ESTHESIS_TOKEN = "X-ESTHESIS-DT-APP";

  @Inject
  @RestClient
  ApplicationSystemResource applicationSystemResource;

  private Optional<String> getToken(ContainerRequestContext requestContext) {
    String authHeader = requestContext.getHeaderString(ESTHESIS_TOKEN);
    if (StringUtils.isNotEmpty(authHeader)) {
      return Optional.of(authHeader);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    Optional<String> token = getToken(requestContext);

    if (token.isEmpty() || (!applicationSystemResource.isTokenValid(token.get()))) {
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }
}
