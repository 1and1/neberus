package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define the allowed values for a parameter. This will be used to replace placeholders in the example curl.
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ApiAllowedValues.class)
public @interface ApiAllowedValue {

    /**
     * The exact allowed values for this parameter.
     *
     * @return the value
     */
    String value() default "";

    /**
     * Hint about the allowed values.
     *
     * @return the valueHint
     */
    String valueHint() default "";

    /**
     * Allow all values of the specified enum.
     *
     * @return the enumClass
     */
    Class<? extends Enum> enumValues() default Enum.class;

}
