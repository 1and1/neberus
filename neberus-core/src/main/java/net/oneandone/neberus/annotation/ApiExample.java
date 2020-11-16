package net.oneandone.neberus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a example for a specific value.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface ApiExample {

    /**
     * Title or description of this example.
     *
     * @return the title
     */
    String title();

    /**
     * The actual example value.
     *
     * @return the value
     */
    String value();

}
