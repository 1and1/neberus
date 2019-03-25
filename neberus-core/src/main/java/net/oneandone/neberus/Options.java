package net.oneandone.neberus;

import java.util.*;

/**
 * Options that may be specified for the doclet
 */
public class Options {

    public String outputDirectory = "";
    public String docBasePath = "";
    public String apiHost = "";
    public String apiBasePath = "";
    public String apiVersion = "";
    public String apiTitle = "";
    public Set<String> scanPackages = new HashSet<>();
    public boolean ignoreErrors = false;
    public Map<String, String> otherOptions = new HashMap<>();

    public static Options parse(String[][] options) {
        Options parsedOptions = new Options();

        for (String[] option : options) {
            switch (option[0]) {
                case "-d":
                    parsedOptions.outputDirectory = option[1] + "/";
                    break;
                case "-docBasePath":
                    parsedOptions.docBasePath = option[1];
                    break;
                case "-apiHost":
                    parsedOptions.apiHost = option[1];
                    break;
                case "-apiBasePath":
                    parsedOptions.apiBasePath = option[1];
                    break;
                case "-apiVersion":
                    parsedOptions.apiVersion = option[1];
                    break;
                case "-apiTitle":
                    parsedOptions.apiTitle = option[1];
                    break;
                case "-scanPackages":
                    parsedOptions.scanPackages = new HashSet<>(Arrays.asList(option[1].split(";")));
                    break;
                case "-ignoreErrors":
                    parsedOptions.ignoreErrors = true;
                    break;
                default:
                    if (option.length == 2) {
                        parsedOptions.otherOptions.put(option[0], option[1]);
                    } else {
                        parsedOptions.otherOptions.put(option[0], "true");
                    }
                    break;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Options other = (Options) obj;

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
