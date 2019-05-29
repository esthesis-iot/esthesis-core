package esthesis.platform.server.config;

import esthesis.extension.config.AppConstants.Generic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", modifyOnCreate = false)
public class DBAuditConfig {

  @Bean
  public AuditorAware<String> auditorProvider() {

    if (SecurityContextHolder.getContext() != null
      && SecurityContextHolder.getContext().getAuthentication() != null
      && SecurityContextHolder.getContext().getAuthentication().getCredentials() != null) {
      return () -> Optional
        .of(SecurityContextHolder.getContext().getAuthentication().getCredentials().toString());
    } else {
      return () -> Optional.of(Generic.SYSTEM);
    }
  }
}
