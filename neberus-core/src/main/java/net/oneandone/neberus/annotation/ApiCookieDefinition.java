package net.oneandone.neberus.annotation;

import net.oneandone.neberus.model.CookieSameSite;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a Cookie on class level, this can be used to provided descriptions of cookies that are reused many times.
 * The cookie name will be used for reference.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ApiCookieDefinitions.class)
@Inherited
public @interface ApiCookieDefinition {

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

}
