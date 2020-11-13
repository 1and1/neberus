package net.oneandone.neberus;

import jdk.javadoc.doclet.DocletEnvironment;

import java.util.*;
import java.util.function.Consumer;

/**
 * Options that may be specified for the doclet
 */
public class Options {

    public DocletEnvironment environment;
    public String outputDirectory = "";
    public String docBasePath = "";
    public String apiHost = "";
    public String apiBasePath = "";
    public String apiVersion = "";
    public String apiTitle = "";
    public Set<String> scanPackages = new HashSet<>();
    public boolean ignoreErrors = false;
    public Map<String, String> otherOptions = new HashMap<>();

    public static Options parse(String[][] options, DocletEnvironment environment) {

        Options parsedOptions = new Options();

        parsedOptions.environment = environment;

        Map<String, Consumer<String>> setters = new HashMap<>();
        setters.put("-d", v -> parsedOptions.outputDirectory = v + "/");
        setters.put("-docBasePath", v -> parsedOptions.docBasePath = v);
        setters.put("-apiHost", v -> parsedOptions.apiHost = v);
        setters.put("-apiBasePath", v -> parsedOptions.apiBasePath = v);
        setters.put("-apiVersion", v -> parsedOptions.apiVersion = v);
        setters.put("-apiTitle", v -> parsedOptions.apiTitle = v);
        setters.put("-scanPackages", v -> parsedOptions.scanPackages = new HashSet<>(Arrays.asList(v.split(";"))));
        setters.put("-ignoreErrors", v -> parsedOptions.ignoreErrors = true);

        for (String[] option : options) {

            if (setters.containsKey(option[0])) {
                setters.get(option[0]).accept(option.length == 2 ? option[1] : null);
            } else {
                if (option.length == 2) {
                    parsedOptions.otherOptions.put(option[0], option[1]);
                } else {
                    parsedOptions.otherOptions.put(option[0], "true");
                }
            }
        }

        System.out.println(parsedOptions.toString());

        if (parsedOptions.equals(new Options())) {
            System.err.println("Cannot find any Options. Please note that 'additionalparam' has been replaced "
                    + "by 'additionalOptions' in maven-javadoc-plugin 3.0.0");
        }

        return parsedOptions;
    }

    @Override
    public String toString() {
        return "Options{" + "outputDirectory=" + outputDirectory + ", docBasePath=" + docBasePath + ", apiHost=" + apiHost
                + ", apiBasePath=" + apiBasePath + ", apiVersion=" + apiVersion + ", apiTitle=" + apiTitle
                + ", scanPackages=" + scanPackages + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputDirectory, docBasePath, apiHost, apiBasePath, apiVersion, apiTitle, scanPackages, ignoreErrors);
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
                && Objects.equals(this.apiHost, other.apiHost)
                && Objects.equals(this.apiBasePath, other.apiBasePath)
                && Objects.equals(this.apiVersion, other.apiVersion)
                && Objects.equals(this.apiTitle, other.apiTitle)
                && Objects.equals(this.scanPackages, other.scanPackages)
                && Objects.equals(this.ignoreErrors, other.ignoreErrors);
    }

}
