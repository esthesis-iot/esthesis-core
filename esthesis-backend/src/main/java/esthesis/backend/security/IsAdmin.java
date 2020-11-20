package esthesis.backend.security;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to use together with {@link OperationsAspect}.
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IsAdmin {
}
