package net.oneandone.neberus.model.problem;

/**
 * Problem URN
 *
 * @author grro
 */
public class ProblemUrn {

    public static final String PREFIX = "urn:problem:ui:";

    // Common client problems
    public static final String CLIENT_PROBLEM_INSUFFICIENT_PERMISSIONS = PREFIX + "insufficient-permissions";
    public static final String CLIENT_PROBLEM_MALFORMED_REQUEST_DATA = PREFIX + "malformed-request-data";

    // Common server problems
    public static final String SERVER_PROBLEM_SUBCALL_ERROR = PREFIX + "subcall-error";
    public static final String SERVER_PROBLEM = PREFIX + "server-error";

    private final String urn;

    /**
     * Constructor using urn.
     *
     * @param urn the urn
     */
    public ProblemUrn(String urn) {
        this.urn = urn;
    }

    /**
     * Get urn.
     *
     * @return the urn
     */
    public String getUrn() {
        return urn;
    }

    @Override
    public String toString() {
        return urn;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProblemUrn && urn.equals(((ProblemUrn) obj).getUrn());
    }

    @Override
    public int hashCode() {
        return urn.hashCode();
    }

}
