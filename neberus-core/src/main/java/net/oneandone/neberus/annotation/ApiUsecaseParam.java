package net.oneandone.neberus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Paramter used by a REST method in a usecase.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiUsecaseParam {

    /**
     * Name of the parameter. If the method is linked to an actual REST method, the name must exist in the linked method.
     * Use dot-syntax for nested parameters, eg. 'myDto.myField'.
     *
     * @return the name
     */
    String name();

    /**
     * Exact value to use. For 'path' and 'query' params this value will be replaced in the shown REST-path and curl.
     *
     * @return the value
     */
    String value() default "";

    /**
     * Hint about the value to be used.
     *
     * @return the value hint
     */
    String valueHint() default "";

}
