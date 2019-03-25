package net.oneandone.neberus.annotation;

import net.oneandone.neberus.model.ProblemType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A single Warning used in {@link ApiWarningResponse}.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiWarning {

    ProblemType type();

    /**
     * The "title" displayed in the Warning. Will be ignored, if no {@link #type() } is set.
     */
    String title() default "";

    /**
     * The "detail" displayed in the Warning. Will be ignored, if no {@link #type() } is set.
     */
    String detail() default "";

}
