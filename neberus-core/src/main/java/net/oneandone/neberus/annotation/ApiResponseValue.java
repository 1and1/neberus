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
@Repeatable(ApiResponseValues.class)
public @interface ApiResponseValue {

    String name();

    String description() default "";

    /**
     * Define the type such as "path" or "query".
     */
    RestMethodData.ParameterType type() default RestMethodData.ParameterType.UNSET;

    /**
     * Defines which values can be returned.
     */
    String allowedValues() default "";

    /**
     * If set, the responseValue will be grouped under the corresponding response entitiy description. Defining a class that is
     * not used for any response entity will cause this responseValue to disappear.
     */
    Class containerClass() default Void.class;

}
