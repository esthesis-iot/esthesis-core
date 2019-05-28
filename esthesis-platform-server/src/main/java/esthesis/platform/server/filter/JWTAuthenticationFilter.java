package esthesis.platform.server.filter;


import static java.util.Collections.emptyList;

import esthesis.platform.server.dto.jwt.JWTClaimsResponseDTO;
import esthesis.platform.server.service.JWTService;
import io.jsonwebtoken.Jwts;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Base64;

@Component
//TODO check OncePerRequestFilter instead
public class JWTAuthenticationFilter extends GenericFilterBean {

  // The name of the token when it comes in the headers.
  private static final String HEADER_NAME = "Authorization";

  // The prefix of the token's value when it comes in the headers.
  private static final String HEADER_TOKEN_PREFIX = "Bearer";

  // The name of the token when it comes as a url param.
  private static final String PARAM_NAME = "bearer";

  private final JWTService jwtService;

  public JWTAuthenticationFilter(JWTService jwtService) {
    this.jwtService = jwtService;
  }

  /**
   * Parses a JWT token (compact or normal) and returns its String representation while it is also validating the
   * token.
   *
   * @param jwt The JWT to decode.
   * @param secret The secret used to sign the JWT.
   * @return Returns the String representation of the JWT.
   */
  private static String tokenToString(String jwt, String secret) {
    return Jwts.parser().setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes())).parse(jwt)
        .toString();
  }

  private static String getRawToken(HttpServletRequest request) {
    // Try to obtain the token from the headers.
    String token = request.getHeader(HEADER_NAME);
    if (StringUtils.isNotBlank(token)) {
      return token.replace(HEADER_TOKEN_PREFIX, "");
    }

    // Next, try to obtain the token from a URL param.
    token = request.getParameter(PARAM_NAME);
    if (StringUtils.isNotBlank(token)) {
      return token;
    }

    // If no token found, return null.
    return null;
  }

  private Authentication getAuthentication(HttpServletRequest request) {
    String jwtToken = getRawToken(request);
    final JWTClaimsResponseDTO jwtClaimsResponseDTO = jwtService.getClaims(jwtToken);
    if (jwtClaimsResponseDTO != null && StringUtils.isNotBlank(jwtClaimsResponseDTO.getSubject())) {
      // Create a Spring Authentication token, with the email address of the user as a Principal
      // and the User Id as credentials).
      return new UsernamePasswordAuthenticationToken(jwtClaimsResponseDTO.getSubject(), jwtToken,
          emptyList());
    } else {
      return null;
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    System.out.println("FILTEr...");
    Authentication authentication = getAuthentication((HttpServletRequest) request);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }
}
