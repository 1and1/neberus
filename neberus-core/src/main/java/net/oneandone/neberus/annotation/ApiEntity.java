package net.oneandone.neberus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a response entity.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface ApiEntity {

    /**
     * Set the class of the DTO used for the entity.
     *
     * @return the entity class
     */
    Class entityClass() default Void.class;

    /**
     * If unset, the first Content-Type defined on the method will be used. Only one entity per Content-Type may be defined.
     *
     * @return the content type
     */
    String contentType() default "";

    /**
     * Description of the entity.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Custom examples for this entity. The examples will be included as is in the documentation without further checks.
     *
     * @return the examples
     */
    ApiExample[] examples() default {};
}
