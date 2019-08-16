package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exclude a request parameter, DTO field or a whole REST method from documentation.
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiIgnore {
}
