package net.oneandone.neberus.annotation;

import net.oneandone.neberus.model.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Document a Response.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ApiResponses.class)
public @interface ApiResponse {

    /**
     * Status of the response.
     *
     * @return the status
     */
    ApiStatus status();

    /**
     * Description of the response.
     *
     * @return the description
     */
    String description();

    /**
     * Entities used in this response.
     *
     * @return the entities
     */
    ApiEntity[] entities() default {};

    /**
     * Headers used in this response.
     *
     * @return the headers
     */
    ApiHeader[] headers() default {};

}
