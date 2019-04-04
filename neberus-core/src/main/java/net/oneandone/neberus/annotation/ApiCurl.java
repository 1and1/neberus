package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells Neberus to generate an example curl.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiCurl {

    /**
     * If provided, this will be included in the documentation and no curl will be generated.
     *
     * @return the value
     */
    String value() default "";

}
