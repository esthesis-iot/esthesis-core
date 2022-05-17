package esthesis.platform.server.config;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.service.ApplicationService;
import java.io.IOException;
import java.text.MessageFormat;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Log
@Component
public class DTSecurityFilter extends OncePerRequestFilter {

  private static final String HEADER_NAME = "Authorization";
  private static final String HEADER_VALUE_PREFIX = "Bearer ";
  private final ApplicationService applicationService;
  private final UserService userService;

  public DTSecurityFilter(ApplicationService applicationService, UserService userService) {
    this.applicationService = applicationService;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {
    RequestMatcher customFilterUrl = new AntPathRequestMatcher("/dt/**");
    if (customFilterUrl.matches(request)) {
      log.finest("Applying filter DTSecurityFilter.");
      // Get the authorization header and remove the Bearer prefix from its value.
      String authHeader = request.getHeader(HEADER_NAME);
      if (StringUtils.isNotEmpty(authHeader) && authHeader.startsWith(HEADER_VALUE_PREFIX)) {
        authHeader = authHeader.substring(HEADER_VALUE_PREFIX.length());
        try {
          final ApplicationDTO application = applicationService.findByToken(authHeader);
          if (application != null) {
            final UserDTO user = userService.getUserById(application.getUserId());
            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId(),
              authHeader, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        } catch (QDoesNotExistException e) {
          log.warning(MessageFormat.format("Did not find a Digital Twin registration for key {0}.",
            authHeader));
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  @SuppressWarnings("unchecked")
  @Bean(name = "DTSecurityFilterRegistrationBean")
  public FilterRegistrationBean registration(DTSecurityFilter filter) {
    FilterRegistrationBean registration = new FilterRegistrationBean(filter);
    registration.setEnabled(false);
    return registration;
  }
}
