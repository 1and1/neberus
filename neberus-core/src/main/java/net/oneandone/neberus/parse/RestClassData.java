package net.oneandone.neberus.parse;

import com.sun.javadoc.ClassDoc;
import net.oneandone.neberus.annotation.ApiDescription;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains data belonging the REST service class.
 */
public class RestClassData {

    public ClassDoc classDoc;
    public String label;
    public String className;

    /**
     * the javadoc comment on the class or, if present, the value of an {@link ApiDescription} annotation on the class.
     */
    public String description;

    public List<RestMethodData> methods;

    /**
     * Class scope defined headers that enable the reuse of the provided header description in the whole class
     */
    public Map<String, RestMethodData.HeaderInfo> headerDefinitions;

    public RestClassData() {
        methods = new ArrayList<>();
        headerDefinitions = new LinkedHashMap<>();
    }

    public void validate(boolean ignoreErrors) {
        HashSet<String> methodLabels = new HashSet<>();

        methods.forEach(method -> {
            if (methodLabels.contains(method.methodData.label)) {
                System.err.println("Method labels must be unique. Found duplicate value <" + method.methodData.label + "> "
                        + "in class <" + className + ">.");
                if (!ignoreErrors) {
                    throw new IllegalArgumentException();
                }
            }

            methodLabels.add(method.methodData.label);
        });
    }

}
