package net.oneandone.neberus.annotation;

import net.oneandone.neberus.ResponseType;
import net.oneandone.neberus.model.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Document a Response that does not fit into Success-, Warning-, or Problem-Response.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiResponses.class)
public @interface ApiResponse {

    ApiStatus status();

    /**
     * Set the class of the DTO used for the response entity. Currently only the first level in a hierarchy will be printed.
     */
    Class entityClass() default Void.class;

    /**
     * If an entityClass is set and this value is unset, the first Content-Type defined in the {@link Produces} annotation will
     * be used.
     */
    String contentType() default "";

    String description() default "";

    ApiHeader[] headers() default {};

    /**
     * Define the type of the response.
     */
    ResponseType responseType();
}
