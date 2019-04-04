package net.oneandone.neberus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a specific usecase of the REST service.
 * Usecases must be defined in seperate classes or interfaces apart from the normal apidoc.
 *
 * The javadoc of the defining class (must be placed above the annotations) will be used as introduction.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiUsecases.class)
public @interface ApiUsecase {

    /**
     * Name of the usecase.
     *
     * @return the name
     */
    String name();

    /**
     * General description of the usecase.
     *
     * @return the description
     */
    String description() default "";

    /**
     * The REST methods to be used for this usecase in order of definition.
     *
     * @return the used methods
     */
    ApiUsecaseMethod[] methods() default {};

}
