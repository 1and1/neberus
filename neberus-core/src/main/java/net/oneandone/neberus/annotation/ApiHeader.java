package net.oneandone.neberus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a response Header within a {@link ApiResponse}.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface ApiHeader {

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

    /**
     * Define allowed value that is returned to the client.
     *
     * @return the allowed value
     */
    ApiAllowedValue[] allowedValues() default {};

    /**
     * If true, the header will be marked as optional, else as mandatory.
     *
     * @return whether the header is optional
     */
    boolean optional() default false;

    /**
     * If true, the header will be marked as deprecated.
     *
     * @return whether the header is deprecated
     */
    boolean deprecated() default false;

    /**
     * Additional description that will be shown if the header is deprecated.
     *
     * @return additional description for a deprecated header
     */
    String deprecatedDescription() default "";

}
