package esthesis.platform.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", modifyOnCreate = false)
public class DBAuditConfig {
  //  private final JWTService jwtService;
  //
  //  public DBAuditConfig(JWTService jwtService) {
  //    this.jwtService = jwtService;
  //  }
  //
  //  @Bean
  //  public AuditorAware<String> auditorProvider() {
  //    return () -> Optional.of(jwtService.getUserId());
  //  }

  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> Optional
      .of(SecurityContextHolder.getContext().getAuthentication().getCredentials().toString());
  }
}
