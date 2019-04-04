package net.oneandone.neberus.annotation;

import net.oneandone.neberus.model.ApiStatus;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Document a Warning Response. The Content-Type will be set to "application/warnings+json".
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiWarningResponses.class)
public @interface ApiWarningResponse {

    ApiStatus status();

    /**
     * Define one or many Warnings that will be shown in an array in the entity.
     *
     * @return the warnings
     */
    ApiWarning[] warnings();

    String description() default "";

    ApiHeader[] headers() default {};
}
