package esthesis.platform.backend.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A bootstrap class to allow component initialization after application has fully started and all
 * Beans are properly configured.
 */
@Log
@Component
@RequiredArgsConstructor
public class Bootstrap {

  private final AppProperties appProperties;

  @EventListener
  public void applicationStarted(ContextRefreshedEvent contextRefreshedEvent) {

  }
}
