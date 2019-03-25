package net.oneandone.neberus.model;

/**
 * All possible problem / warning types.
 */
public enum ProblemType {

    AUTHENTICATION_ERROR("authentication-error"),
    BUSINESS_LOCKED("business-locked"),
    CORRUPTED_DATA("corrupted-data"),
    CORRUPTED_REQUEST_DATA("corrupted-request-data"),
    EXPECTATION_FAILED("expectation-failed"),
    INSTANCE_ALREADY_EXISTS("instance-already-exists"),
    INSTANCE_NOT_EXISTS("instance-not-exists"),
    INSUFFICIENT_PERMISSIONS("insufficient-permissions"),
    INVALID_INSTANCE_STATE("invalid-instance-state"),
    LIMIT_EXCEEDED("limit-exceeded"),
    MALFORMED_REQUEST_DATA("malformed-request-data"),
    MALFORMED_RESOURCE_IDENTIFIER("malformed-resource-identifier"),
    METHOD_NOT_ALLOWED("method-not-allowed"),
    MISSING_REQUEST_DATA("missing-request-data"),
    REQUEST_TOO_LARGE("request-too-large"),
    RESOURCE_UNAVAILABLE("resource-unavailable"),
    RESPONSE_TOO_LARGE("response-too-large"),
    SERVER_ERROR("server-error"),
    SUBCALL_AUTH_ERROR("subcall-auth-error"),
    SUBCALL_ERROR("subcall-error"),
    TECHNICAL_LOCKED("technical-locked"),
    TOO_MANY_REQUESTS("too-many-requests");

    public final String value;

    ProblemType(String value) {
        this.value = value;
    }

}
