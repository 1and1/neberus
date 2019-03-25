package net.oneandone.neberus.annotation;

import net.oneandone.neberus.parse.RestMethodData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a custom parameter for a REST method. Most likely used to document fields inside the body.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiParameters.class)
public @interface ApiParameter {

    String name();

    String description() default "";

    /**
     * Define the type such as "path", "query" or "header".
     */
    RestMethodData.ParameterType type() default RestMethodData.ParameterType.UNSET;

    /**
     * Define allowed values that should be provided by the client. If missing, the possible value will be generated.
     * In case of an enum, all enum values will be shown, otherwise it will be something like {String}.
     */
    String allowedValues() default "";

    /**
     * If set, the parameter will be grouped under the corresponding request entitiy description. Defining a class that is not
     * used for any request entity will cause this parameter to disappear.
     */
    Class containerClass() default Void.class;

    /**
     * Define the class of the parameter. May be used for templates and as containerClass for other paramters.
     */
    Class entityClass() default Void.class;

}
