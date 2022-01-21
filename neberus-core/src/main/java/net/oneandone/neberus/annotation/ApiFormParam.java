package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare a @RequestParam annotated parameter as form parameter instead of query parameter.
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.SOURCE)
public @interface ApiFormParam {
}
