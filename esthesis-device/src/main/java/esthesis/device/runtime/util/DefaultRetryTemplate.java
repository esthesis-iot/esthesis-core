package esthesis.device.runtime.util;

import esthesis.device.runtime.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DefaultRetryTemplate {

  private final AppProperties appProperties;

  public DefaultRetryTemplate(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  /**
   * Specify a RetryTemplate to be used while making requests.
   */
  @Bean
  public RetryTemplate retryTemplate() {
    // Define retry policy.
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(appProperties.getRequestAttempts());

    // Define backoff policy.
    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(appProperties.getRequestRetryBackoff());
    backOffPolicy.setMaxInterval(TimeUnit.MINUTES.toMillis(appProperties.getRequestMaxBackoff()));
    backOffPolicy.setMultiplier(2);

    // Define retry template.
    RetryTemplate template = new RetryTemplate();
    template.setRetryPolicy(retryPolicy);
    template.setBackOffPolicy(backOffPolicy);

    return template;
  }
}
