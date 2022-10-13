package esthesis.service.common.paging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageReplyFilter {

  String filter() default "*";
}
