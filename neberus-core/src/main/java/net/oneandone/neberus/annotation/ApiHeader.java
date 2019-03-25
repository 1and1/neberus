package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a Header.<br>
 * If defined within a {@link ApiResponse}, it will represent a response header.<br>
 * If defined on class level, this can be used to provied descriptions of headers that are reused many times. The header name will
 * be used for reference.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiHeaders.class)
public @interface ApiHeader {

    /**
     * Name of the header. May be used on a method to reference a header defined on the class.
     */
    String name();

    String description() default "";

}
