package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define roles/authorities required to access.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface ApiAllowedRoles {

    String[] value();

}
