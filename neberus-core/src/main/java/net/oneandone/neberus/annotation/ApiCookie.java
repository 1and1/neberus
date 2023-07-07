package net.oneandone.neberus.annotation;

import net.oneandone.neberus.model.CookieSameSite;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a response cookie within a {@link ApiResponse}.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface ApiCookie {

    /**
     * Name of the cookie. May be used on a method to reference a cookie defined on the class.
     *
     * @return the name
     */
    String name();

    /**
     * Description of the cookie.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Cookie-Option SameSite
     */
    CookieSameSite sameSite() default CookieSameSite.UNSET;

    /**
     * Cookie-Option Domain
     */
    String domain() default "";

    /**
     * Cookie-Option Path
     */
    String path() default "";

    /**
     * Cookie-Option Max-Age
     */
    String maxAge() default "";

    /**
     * Cookie-Option Secure
     */
    boolean secure() default false;

    /**
     * Cookie-Option HttpOnly
     */
    boolean httpOnly() default false;

    /**
     * Define allowed value that is returned to the client.
     *
     * @return the allowed value
     */
    ApiAllowedValue[] allowedValues() default {};

    /**
     * If true, the cookie will be marked as optional, else as mandatory.
     *
     * @return whether the cookie is optional
     */
    boolean optional() default false;

    /**
     * If true, the cookie will be marked as deprecated.
     *
     * @return whether the cookie is deprecated
     */
    boolean deprecated() default false;

    /**
     * Additional description that will be shown if the cookie is deprecated.
     *
     * @return additional description for a deprecated cookie
     */
    String deprecatedDescription() default "";

}
