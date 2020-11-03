package esthesis.platform.server.config;

import com.eurodyn.qlack.common.exception.QAuthenticationException;
import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.service.ApplicationService;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Log
@Component
public class DTSecurityFilter extends GenericFilterBean {

  private static final String X_HEADER_NAME = "X-ESTHESIS-DT-AUTH";
  private final ApplicationService applicationService;
  private final UserService userService;

  public DTSecurityFilter(ApplicationService applicationService,
    UserService userService) {
    this.applicationService = applicationService;
    this.userService = userService;
  }

  @Override
  public void doFilter(ServletRequest request,
    ServletResponse response, FilterChain filterChain)
  throws IOException, ServletException {
    log.finest("Applying filter DTSecurityFilter.");

    String authHeader =
      ((HttpServletRequest) request).getHeader(X_HEADER_NAME);
    if (StringUtils.isNotEmpty(authHeader)) {
      final ApplicationDTO application = applicationService.findByToken(authHeader);
      if (application != null) {
        final UserDTO user = userService.getUserById(application.getUserId());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
          user.getId(), authHeader, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
    } else {
      ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      throw new QAuthenticationException(
        "You have no permission to access the Digital Twin interface.");
    }
  }

  @Bean(name = "DTSecurityFilterRegistrationBean")
  public FilterRegistrationBean registration(DTSecurityFilter filter) {
    FilterRegistrationBean registration = new FilterRegistrationBean(filter);
    registration.setEnabled(false);
    return registration;
  }
}
