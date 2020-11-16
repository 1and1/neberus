package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If provided, the value of this will be used as description of the method instead of the javadoc comment.
 * If defined on a class, this will be used as short description of the resource on the service overview page.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface ApiDescription {

    /**
     * @return the description
     */
    String value();

}
