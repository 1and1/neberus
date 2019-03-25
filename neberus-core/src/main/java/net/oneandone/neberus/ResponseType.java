package net.oneandone.neberus;

public enum ResponseType {

    /**
     * Successful response. Will be shown in green.
     */
    SUCCESS,
    /**
     * Successful response with warnings. Will be shown in yellow.
     */
    WARNING,
    /**
     * Problem/Error response. Will be shown in red.
     */
    PROBLEM,
    /**
     * Generic response. Will be shown in gray.
     */
    GENERIC

}
