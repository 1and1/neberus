package net.oneandone.neberus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Request body for a REST method in a usecase.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface ApiUsecaseRequestBody {

    String contentType();

    /**
     * Exact returned value.
     *
     * @return the value
     */
    String value();

    /**
     * Hint about the value to be used.
     *
     * @return the value hint
     */
    String valueHint() default "";

}
