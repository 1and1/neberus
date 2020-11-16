package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for {@link ApiHeader}s. Not required to be explicitly used with Java 8.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface ApiHeaders {

    ApiHeader[] value();

}
