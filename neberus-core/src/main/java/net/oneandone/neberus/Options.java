package net.oneandone.neberus;

import jdk.javadoc.doclet.DocletEnvironment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Options that may be specified for the doclet
 */
public class Options {

    public DocletEnvironment environment;
    public String outputDirectory = "";
    public String docBasePath = "";
    public List<String> apiHosts = new ArrayList<>();
    public String apiBasePath = "";
    public String apiVersion = "";
    public String apiTitle = "";
    public Set<String> scanPackages = new HashSet<>();
    public boolean ignoreErrors = false;

    @Override
    public String toString() {
        return "Options{" + "outputDirectory=" + outputDirectory + ", docBasePath=" + docBasePath + ", apiHosts=" + apiHosts
                + ", apiBasePath=" + apiBasePath + ", apiVersion=" + apiVersion + ", apiTitle=" + apiTitle
                + ", scanPackages=" + scanPackages + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputDirectory, docBasePath, apiHosts, apiBasePath, apiVersion, apiTitle, scanPackages, ignoreErrors);
    }

    @SuppressWarnings("CyclomaticComplexity")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Options other = (Options) obj;

        return allFieldsAreEqual(other);
    }

    private boolean allFieldsAreEqual(Options other) {
        return Objects.equals(this.outputDirectory, other.outputDirectory)
                && Objects.equals(this.docBasePath, other.docBasePath)
                && Objects.equals(this.apiHosts, other.apiHosts)
                && Objects.equals(this.apiBasePath, other.apiBasePath)
                && Objects.equals(this.apiVersion, other.apiVersion)
                && Objects.equals(this.apiTitle, other.apiTitle)
                && Objects.equals(this.scanPackages, other.scanPackages)
                && Objects.equals(this.ignoreErrors, other.ignoreErrors);
    }

}
