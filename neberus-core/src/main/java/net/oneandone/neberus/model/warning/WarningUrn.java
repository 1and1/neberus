package net.oneandone.neberus.model.warning;

import net.oneandone.neberus.model.problem.ProblemUrn;
import java.util.Objects;

/**
 * Adapted from {@link ProblemUrn}.
 */
public class WarningUrn {

    public static final String PREFIX = "urn:warning:ui.";

    private final String urn;

    /**
     * Constructor using urn.
     *
     * @param urn the urn
     */
    public WarningUrn(String urn) {
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
        return obj instanceof WarningUrn && Objects.equals(urn, ((WarningUrn) obj).getUrn());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(urn);
    }
}
