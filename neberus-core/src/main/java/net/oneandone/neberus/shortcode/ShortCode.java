package net.oneandone.neberus.shortcode;

/**
 * [[key][value]]
 */
public abstract class ShortCode {

    public abstract String getKey();

    public abstract String process(String value);

}
