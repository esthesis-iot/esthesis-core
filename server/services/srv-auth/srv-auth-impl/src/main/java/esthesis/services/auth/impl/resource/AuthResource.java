package esthesis.services.auth.impl.resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Path("/api/v1/auth")
public class AuthResource {

  private static final String ACCESS_TOKEN_HEADER = "X-Access-Token";
  private static final String ID_TOKEN_HEADER = "X-ID-Token";
  private static final String USER_INFO_TOKEN_HEADER = "X-Userinfo";
  private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
  private static final String ORIGIN_HEADER = "X-Origin";

  @GET
  @Path("/refresh")
  @Produces(MediaType.APPLICATION_JSON)
  public Response refreshToken() {
    return Response.ok("test").build();
  }

  @GET
  public Response auth(HttpHeaders headers, @QueryParam("origin") String origin)
  throws URISyntaxException {
    // Iterate over http headers.
    for (String header : headers.getRequestHeaders().keySet()) {
      log.info("Header {}: {}", header,
          headers.getRequestHeaders().get(header));
    }

    String accessToken = headers.getHeaderString(ACCESS_TOKEN_HEADER);
    String idToken = headers.getHeaderString(ID_TOKEN_HEADER);
    String userInfo = headers.getHeaderString(USER_INFO_TOKEN_HEADER);
    String refreshToken = headers.getHeaderString(REFRESH_TOKEN_HEADER);

    log.debug("accessToken: {}", accessToken);
    log.debug("idToken: {}", idToken);
    log.debug("userInfo: {}", userInfo);
    log.debug("refreshToken: {}", refreshToken);
    log.debug("origin: {}", origin);

    StringBuffer redirectURL = new StringBuffer();
    if (StringUtils.isNotEmpty(origin)) {
      redirectURL.append(origin);
    }
    if (redirectURL.length() > 0) {
      if (!redirectURL.toString().contains("?")) {
        redirectURL.append("?");
      } else {
        redirectURL.append("&");
      }
    }
    redirectURL.append(ACCESS_TOKEN_HEADER);
    redirectURL.append("=");
    redirectURL.append(accessToken);
    redirectURL.append("&");
    redirectURL.append(ID_TOKEN_HEADER);
    redirectURL.append("=");
    redirectURL.append(idToken);
    if (StringUtils.isNotEmpty(userInfo)) {
      redirectURL.append("&");
      redirectURL.append(USER_INFO_TOKEN_HEADER);
      redirectURL.append("=");
      redirectURL.append(userInfo);
    }
    if (StringUtils.isNotEmpty(refreshToken)) {
      redirectURL.append("&");
      redirectURL.append(REFRESH_TOKEN_HEADER);
      redirectURL.append("=");
      redirectURL.append(refreshToken);
    }
    if (StringUtils.isNotEmpty(origin)) {
      redirectURL.append("&");
      redirectURL.append(ORIGIN_HEADER);
      redirectURL.append("=");
      redirectURL.append(URLEncoder.encode(origin, StandardCharsets.UTF_8));
    }

    log.debug("Redirecting to: {}.", redirectURL);

    return Response.temporaryRedirect(new URI(redirectURL.toString())).build();
  }
}
