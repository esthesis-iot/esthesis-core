package esthesis.platform.server.config;

import com.eurodyn.qlack.util.jwt.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String[] PUBLIC_URIS =
    {"/users/auth", "/ping", "/agent/**"};
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final DTSecurityFilter dtSecurityFilter;

  public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
    DTSecurityFilter dtSecurityFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.dtSecurityFilter = dtSecurityFilter;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .csrf().disable()
      .authorizeRequests()
      .antMatchers(PUBLIC_URIS).permitAll()
      .anyRequest().authenticated()
      .and()
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterBefore(dtSecurityFilter, UsernamePasswordAuthenticationFilter.class)
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }
}
