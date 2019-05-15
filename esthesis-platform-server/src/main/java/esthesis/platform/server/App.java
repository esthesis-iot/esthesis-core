package esthesis.platform.server;

import javax.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.Security;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@EntityScan({"esthesis", "com.eurodyn.qlack"})
@ComponentScan({"esthesis", "com.eurodyn.qlack"})
@EnableJpaRepositories({"esthesis", "com.eurodyn.qlack"})
public class App {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(App.class.getName());

  @PostConstruct
  void started() {
    LOGGER.log(Level.INFO, "Timezone name  : " + TimeZone.getDefault().getDisplayName());
    LOGGER.log(Level.INFO, "Timezone ID    : " + TimeZone.getDefault().getID());
    LOGGER.log(Level.INFO, "Timezone offset: "
        + TimeUnit.MILLISECONDS.toMinutes(TimeZone.getDefault().getRawOffset()) + " minutes");
  }

  /**
   * Main application start interface.
   */
  public static void main(String[] args) {
    // Add BouncyCastle security provider.
    Security.addProvider(new BouncyCastleProvider());

    // Start the application.
    SpringApplication.run(App.class, args);
  }
}
