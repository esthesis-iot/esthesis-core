package esthesis.services.application.impl.security;

import esthesis.service.application.security.DTSecurityFilter;
import esthesis.services.application.impl.service.ApplicationService;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Provider
@DTSecurityFilter
public class DTSecurityFilterProvider implements ContainerRequestFilter {

  private static final String ESTHESIS_TOKEN = "X-ESTHESIS-DT-APP";

  @Inject
  ApplicationService applicationService;

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

    if (!token.isPresent() || (!applicationService.isTokenValid(token.get()))) {
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }
}
