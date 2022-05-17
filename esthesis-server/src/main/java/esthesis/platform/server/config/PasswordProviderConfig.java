package esthesis.platform.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class PasswordProviderConfig {

  @Bean
  @Primary
  public PasswordEncoder getPasswordEncoder() throws NoSuchAlgorithmException {
    return new BCryptPasswordEncoder(10, SecureRandom.getInstanceStrong());
  }
}
