package esthesis.platform.backend.server;

import javax.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Security;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@EntityScan({"esthesis", "com.eurodyn.qlack"})
@ComponentScan({"esthesis", "com.eurodyn.qlack"})
@EnableJpaRepositories({"esthesis", "com.eurodyn.qlack"})
public class EsthesisPlatform {

  @PostConstruct
  void started() {
    log.log(Level.INFO, "Timezone name  : {0}}", TimeZone.getDefault().getDisplayName());
    log.log(Level.INFO, "Timezone ID    : {0}}", TimeZone.getDefault().getID());
    log.log(Level.INFO, "Timezone offset: {0}} minutes",
      TimeUnit.MILLISECONDS.toMinutes(TimeZone.getDefault().getRawOffset()));
  }

  /**
   * Main application start interface.
   */
  @SuppressWarnings("java:S4823")
  public static void main(String[] args) {
    // Add BouncyCastle security provider.
    Security.addProvider(new BouncyCastleProvider());

    // Setup Security Context strategy to work with asynchronous events processing.
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

    // Start the application.
    SpringApplication.run(EsthesisPlatform.class, args);
  }
}
