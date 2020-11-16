package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a Header on class level, this can be used to provided descriptions of headers that are reused many times. The header name will
 * be used for reference.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ApiHeaderDefinitions.class)
@Inherited
public @interface ApiHeaderDefinition {

    /**
     * Name of the header. May be used on a method to reference a header defined on the class.
     *
     * @return the name
     */
    String name();

    /**
     * Description of the header.
     *
     * @return the description
     */
    String description() default "";

}
