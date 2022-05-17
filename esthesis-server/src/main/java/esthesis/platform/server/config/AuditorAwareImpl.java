package esthesis.platform.server.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import esthesis.platform.server.config.AppConstants.Audit.Generic;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    if (SecurityContextHolder.getContext() != null
      && SecurityContextHolder.getContext().getAuthentication() != null
      && SecurityContextHolder.getContext().getAuthentication().getCredentials() != null) {
      return Optional
        .of(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    } else {
      return Optional.of(Generic.SYSTEM);
    }
  }

}
