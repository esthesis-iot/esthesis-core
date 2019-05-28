package esthesis.platform.server.config;

import esthesis.platform.server.service.JWTService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", modifyOnCreate = false)
public class DBAuditConfig {
  private final JWTService jwtService;

  public DBAuditConfig(JWTService jwtService) {
    this.jwtService = jwtService;
  }

  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> Optional.of(jwtService.getUserId());
  }
}
