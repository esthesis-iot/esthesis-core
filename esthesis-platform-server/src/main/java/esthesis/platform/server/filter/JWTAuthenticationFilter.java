package esthesis.platform.server.filter;


import static esthesis.platform.server.config.WebSecurityConfig.PUBLIC_URIS;
import static java.util.Collections.emptyList;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import esthesis.platform.server.config.AppConstants;
import esthesis.platform.server.config.AppConstants.Jwt;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.dto.jwt.JWTClaimsResponseDTO;
import esthesis.platform.server.service.ApplicationService;
import esthesis.platform.server.service.JWTService;
import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(JWTAuthenticationFilter.class.getName());

  private final UserService userService;
  private final ApplicationService applicationService;

  // The prefix of the token's value when it comes in the headers.
  // A JWT logged-in user.
  private static final String BEARER_HEADER = "Bearer";

  // The name of the token when it comes as a url param.
  private static final String BEARER_PARAM = "bearer";

  private final JWTService jwtService;
  private AntPathMatcher antPathMatcher;

  @Value("${server.servlet.context-path}")
  private String contextRoot;

  private List<String> PUBLIC_URIS_PREFIXED;

  public JWTAuthenticationFilter(UserService userService,
    ApplicationService applicationService, JWTService jwtService) {
    this.userService = userService;
    this.applicationService = applicationService;
    this.jwtService = jwtService;
    antPathMatcher = new AntPathMatcher();
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
    String authHeader = request.getHeader(AppConstants.Http.AUTHORIZATION);

    if (StringUtils.isNotBlank(authHeader)) {
      if (authHeader.startsWith(BEARER_HEADER)) {
        return decodeJwt(authHeader.replace(BEARER_HEADER, "").trim());
      }
      if (authHeader.startsWith(esthesis.common.config.AppConstants.XESTHESISDT_HEADER)) {
        String token = authHeader.replace(esthesis.common.config.AppConstants.XESTHESISDT_HEADER, "").trim();
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

  @PostConstruct
  public void prefixPublicURLs() {
    // Prefix public URIs with context root, so that they can be checked against incoming requests.
    PUBLIC_URIS_PREFIXED = Arrays.stream(PUBLIC_URIS).map(s -> contextRoot + s)
      .collect(Collectors.toList());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {
    // Filter private resources.
    if (PUBLIC_URIS_PREFIXED.stream()
      .noneMatch(p -> antPathMatcher.match(p, ((HttpServletRequest) request).getRequestURI()))) {
      Authentication authentication = getAuthentication((HttpServletRequest) request);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Proceed with other filters.
    filterChain.doFilter(request, response);
  }
}
