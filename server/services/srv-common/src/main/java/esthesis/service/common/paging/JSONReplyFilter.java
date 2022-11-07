package esthesis.service.common.paging;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;

/**
 * Dynamics filtering of JSON results in REST as per
 * https://github.com/Antibrumm/jackson-antpathfilter
 * <p>
 * NOTE: Due to the way Quarkus handles annotations, for this annotation to work
 * you need to have both your REST Interface and Implementation Class annotated
 * with the verb/method and path annotations (e.g. @GET, @Path). Relevant
 * discussion: https://github.com/quarkusio/quarkus/discussions/26020
 */
@Documented
@NameBinding
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface JSONReplyFilter {

  String filter() default "*";
}
