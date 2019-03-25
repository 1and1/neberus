package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define the allowed values for a parameter. This will be used to replace placeholders in the example curl.
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiAllowedValues {

    /**
     * Name or describe the allowed values for this parameter.
     */
    String value() default "";

}
