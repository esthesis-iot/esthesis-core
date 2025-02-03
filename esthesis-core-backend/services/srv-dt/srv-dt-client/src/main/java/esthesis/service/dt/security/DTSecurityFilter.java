package esthesis.service.dt.security;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for application token-based security filter.
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface DTSecurityFilter {

}
