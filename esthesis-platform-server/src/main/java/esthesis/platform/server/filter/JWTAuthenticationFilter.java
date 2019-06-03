package esthesis.platform.server.filter;


import static java.util.Collections.emptyList;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import esthesis.platform.server.config.AppConstants.Jwt;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.dto.jwt.JWTClaimsResponseDTO;
import esthesis.platform.server.service.ApplicationService;
import esthesis.platform.server.service.JWTService;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
//TODO check OncePerRequestFilter instead
public class JWTAuthenticationFilter extends GenericFilterBean {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(JWTAuthenticationFilter.class.getName());

  private final UserService userService;
  private final ApplicationService applicationService;

  // The prefix of the token's value when it comes in the headers.
  // A JWT logged-in user.
  private static final String BEARER_HEADER = "Bearer";
  // An esthesis Digital Twins app.
  private static final String XESTHESISDT_HEADER = "X-ESTHESIS-DT";

  // The name of the token when it comes as a url param.
  private static final String BEARER_PARAM = "bearer";

  private final JWTService jwtService;

  public JWTAuthenticationFilter(UserService userService,
    ApplicationService applicationService, JWTService jwtService) {
    this.userService = userService;
    this.applicationService = applicationService;
    this.jwtService = jwtService;
  }

  private UsernamePasswordAuthenticationToken decodeJwt(String jwtToken) {
    final JWTClaimsResponseDTO jwtClaimsResponseDTO = jwtService.getClaims(jwtToken);
    if (jwtClaimsResponseDTO != null && StringUtils.isNotBlank(jwtClaimsResponseDTO.getSubject())) {
      return new UsernamePasswordAuthenticationToken(jwtClaimsResponseDTO.getSubject(),
        jwtClaimsResponseDTO.getClaims().get(Jwt.JWT_CLAIM_USER_ID), emptyList());
    } else {
      return null;
    }
  }

  private Authentication getAuthentication(HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (StringUtils.isNotBlank(authHeader)) {
      if (authHeader.startsWith(BEARER_HEADER)) {
        return decodeJwt(authHeader.replace(BEARER_HEADER, "").trim());
      }
      if (authHeader.startsWith(XESTHESISDT_HEADER)) {
        String token = authHeader.replace(XESTHESISDT_HEADER, "").trim();
        try {
          final ApplicationDTO applicationDTO = applicationService.findByToken(token);
          if (applicationDTO.isState()) {
            final UserDTO userDTO = userService.getUserById(applicationDTO.getUserId());
            return new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getId(),
              emptyList());
          } else {
            return null;
          }
        } catch (QDoesNotExistException e) {
          LOGGER.log(Level.WARNING, MessageFormat.format("Could not find application with token "
            + "{0}.", token));
          return null;
        }
      } else {
        LOGGER.log(Level.WARNING, "An unknown authorisation header was found: {0}.", authHeader);
        return null;
      }
    } else if (StringUtils.isNotBlank(request.getParameter(BEARER_PARAM))) {
      return decodeJwt(request.getParameter(BEARER_PARAM));
    } else {
      return null;
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
  throws IOException, ServletException {
    Authentication authentication = getAuthentication((HttpServletRequest) request);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }
}
