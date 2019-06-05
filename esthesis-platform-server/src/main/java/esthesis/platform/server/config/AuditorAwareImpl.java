package esthesis.platform.server.config;

import esthesis.extension.common.config.AppConstants.Generic;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    if (SecurityContextHolder.getContext() != null
      && SecurityContextHolder.getContext().getAuthentication() != null
      && SecurityContextHolder.getContext().getAuthentication().getCredentials() != null) {
      return Optional
        .of(SecurityContextHolder.getContext().getAuthentication().getCredentials().toString());
    } else {
      return Optional.of(Generic.SYSTEM);
    }
  }

}
