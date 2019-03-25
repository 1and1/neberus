package net.oneandone.neberus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A specific REST method used in an usecase.
 * If the method is documented within the same service, it can be referenced by providing the restClass and name (label) of the
 * method. In this case a link will be created and all parameters and responseValues will be cross-checked so they actually
 * exist in the documented method.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiUsecaseMethod {

    /**
     * The name of the method. If {@link #restClass() } is defined, this must match the value of a {@link ApiLabel } of one
     * method in the linked class
     */
    String name();

    /**
     * Description of the usage of this method
     */
    String description();

    /**
     * The REST class containing the referenced method
     */
    Class restClass() default Void.class;

    /**
     * Usage explanation of the parameters
     */
    ApiUsecaseParam[] parameters() default {};

    /**
     * Usage explanation of the response value
     */
    ApiUsecaseResponseValue[] responseValue() default {};

}
