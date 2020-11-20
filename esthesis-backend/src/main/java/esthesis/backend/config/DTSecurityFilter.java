package esthesis.backend.config;

import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import esthesis.backend.dto.ApplicationDTO;
import esthesis.backend.service.ApplicationService;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
import java.text.MessageFormat;

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
      try {
        final ApplicationDTO application = applicationService.findByToken(authHeader);
        if (application != null) {
          final UserDTO user = userService.getUserById(application.getUserId());
          Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getId(), authHeader, null);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (QDoesNotExistException e) {
        log.warning(MessageFormat.format("Did not find a Digital Twin registration for key {0}.", authHeader));
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
