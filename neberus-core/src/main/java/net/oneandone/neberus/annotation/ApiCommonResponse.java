package net.oneandone.neberus.annotation;

import net.oneandone.neberus.model.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Document a common Response on class level, the response will be added to every method inside that class.
 * An @ApiResponse defined on the method with the same status will be favoured.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ApiCommonResponses.class)
@Inherited
public @interface ApiCommonResponse {

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
