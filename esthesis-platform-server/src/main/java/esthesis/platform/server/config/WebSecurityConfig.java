package esthesis.platform.server.config;

import esthesis.platform.server.filter.JWTAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final JWTAuthenticationFilter jwtAuthenticationFilter;

  public WebSecurityConfig(JWTAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable().authorizeRequests()
        .antMatchers("/users/auth").permitAll()
        .antMatchers("/actuator/*").permitAll()
        .antMatchers("/acl/mqtt/*").permitAll()
        .antMatchers("/actuator").permitAll()
        .antMatchers("/devices/register").permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
  }
}
