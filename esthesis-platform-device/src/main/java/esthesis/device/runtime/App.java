package esthesis.device.runtime;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@ComponentScan({"esthesis", "com.eurodyn.qlack"})
public class App {

  // proxyWeb needs special handling here, since although defined in AppProperties it can not be
  // accessed at this stage (i.e. before Spring is initialised).
  private static final String PROXY_WEB = "proxyWeb";

  /**
   * Main application start interface.
   */
  @SuppressWarnings({"java:S4823", "java:S5304"})
  public static void main(String[] args) {
    SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(App.class)
      .logStartupInfo(false);

    // Check if the embedded Web server should be started.
    if (BooleanUtils.toBooleanDefaultIfNull(Boolean.valueOf(System.getenv(PROXY_WEB)), false)) {
      springApplicationBuilder.web(WebApplicationType.SERVLET);
    } else {
      springApplicationBuilder.web(WebApplicationType.NONE);
    }

    // Start the Spring Boot app.
    springApplicationBuilder.run(args);
  }
}
