package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define the type that should be displayed instead of the actual type. This can be used to hide internal wrapper DTOs.
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiType {

    /**
     * The type to be displayed instead.
     */
    Class<?> value();

}
