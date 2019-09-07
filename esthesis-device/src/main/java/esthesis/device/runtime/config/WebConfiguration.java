package esthesis.device.runtime.config;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class WebConfiguration implements
  WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

  private final AppProperties appProperties;

  public WebConfiguration(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  @Override
  public void customize(ConfigurableServletWebServerFactory server) {
    server.setPort(appProperties.getProxyWebPort());
  }
}
