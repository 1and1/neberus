package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a request entity for a specific content-type.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ApiRequestEntities.class)
public @interface ApiRequestEntity {

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
     * Description of the request entity.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Type of the parameters. For QUERY, the {@link #contentType()} is ignored.
     *
     * @return the type of the parameters contained in {@link #entityClass()}
     */
    Type type() default Type.BODY;

    /**
     * Custom examples for this entity. The examples will be included as is in the documentation without further checks.
     *
     * @return the examples
     */
    ApiExample[] examples() default {};

    enum Type {
        QUERY, BODY
    }
}
