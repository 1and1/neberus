package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container for multiple {@link ApiUsecase} annotations.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface ApiUsecases {

    ApiUsecase[] value();

}
