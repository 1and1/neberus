package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a custom parameter for a REST method.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ApiParameters.class)
public @interface ApiParameter {

    /**
     * The name of the parameter.
     *
     * @return the name
     */
    String name();

    /**
     * Description of the parameter.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Define the type such as "path", "query" or "header".
     *
     * @return the type
     */
    Type type();

    /**
     * Define allowed value that should be provided by the client. If missing, the possible value will be generated.
     * In case of an enum, all enum values will be shown, otherwise it will be something like {String}.
     *
     * @return the allowed value
     */
    ApiAllowedValues[] allowedValues() default {};

    /**
     * Define the class of the parameter. May be used for templates and as containerClass for other parameters.
     *
     * @return the entity class
     */
    Class entityClass() default Void.class;

    /**
     * If true, the parameter will be marked as optional, else as mandatory.
     *
     * @return whether the parameter is optional
     */
    boolean optional() default false;

    /**
     * If true, the parameter will be marked as deprecated.
     *
     * @return whether the parameter is deprecated
     */
    boolean deprecated() default false;

    /**
     * Additional description that will be shown if the parameter is deprecated.
     *
     * @return additional description for a deprecated parameter
     */
    String deprecatedDescription() default "";

    enum Type {
        PATH, QUERY, HEADER
    }

}
