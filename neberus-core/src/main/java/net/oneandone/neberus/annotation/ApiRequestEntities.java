package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for @ApiRequestEntity.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface ApiRequestEntities {
    ApiRequestEntity[] value();
}
