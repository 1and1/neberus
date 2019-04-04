package net.oneandone.neberus.annotation;

import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.model.ProblemType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Document a Problem Response. The Content-Type will be set to "application/problem+json".
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiProblemResponses.class)
public @interface ApiProblemResponse {

    ApiStatus status();

    ProblemType type();

    /**
     * The "title" displayed in the Problem. Will be ignored, if no {@link #type() } is set.
     *
     * @return the title
     */
    String title() default "";

    /**
     * The "detail" displayed in the Problem. Will be ignored, if no {@link #type() } is set.
     *
     * @return the detail
     */
    String detail() default "";

    String description() default "";

    ApiHeader[] headers() default {};
}
