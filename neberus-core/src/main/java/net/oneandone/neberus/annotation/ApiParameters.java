package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for {@link ApiParameter}s. Not required to be explicitly used with Java 8.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface ApiParameters {
    
    ApiParameter[] value();

}
