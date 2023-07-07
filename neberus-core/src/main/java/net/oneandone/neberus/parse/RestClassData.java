package net.oneandone.neberus.parse;

import net.oneandone.neberus.annotation.ApiDescription;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains data belonging the REST service class.
 */
public class RestClassData {

    public TypeElement classDoc;
    public String label;
    public String className;

    /**
     * the javadoc comment on the class
     */
    public String description;

    /**
     * the value of an {@link ApiDescription} annotation on the class
     */
    public String shortDescription;

    public List<RestMethodData> methods;

    /**
     * Class scope defined headers that enable the reuse of the provided header description in the whole class
     */
    public Map<String, RestMethodData.HeaderInfo> headerDefinitions;

    /**
     * Class scope defined cookies that enable the reuse of the provided cookie description in the whole class
     */
    public Map<String, RestMethodData.CookieInfo> cookieDefinitions;

    /**
     * Class scope defined responses that will be added to every method in the whole class.
     */
    public List<RestMethodData.ResponseData> commonResponseData;

    public RestClassData() {
        methods = new ArrayList<>();
        headerDefinitions = new LinkedHashMap<>();
        cookieDefinitions = new LinkedHashMap<>();
        commonResponseData = new LinkedList<>();
    }

    public void validate(boolean ignoreErrors) {
        Map<String, ExecutableElement> methodLabels = new HashMap<>();

        methods.forEach(method -> {
            if (methodLabels.containsKey(method.methodData.label)) {
                if (method.methodData.methodDoc == methodLabels.get(method.methodData.label)) {
                    return; // origin from same method is ok
                }

                System.err.println("Method labels must be unique. Found duplicate value <" + method.methodData.label + "> "
                        + "in class <" + className + ">.\nConflicting:\n" + method.methodData.methodDoc + "\nand\n"
                        + methodLabels.get(method.methodData.label));

                if (!ignoreErrors) {
                    throw new IllegalArgumentException();
                }
            }

            methodLabels.put(method.methodData.label, method.methodData.methodDoc);
        });
    }

    @Override
    public String toString() {
        return "RestClassData{" +
                "classDoc=" + classDoc +
                ", label='" + label + '\'' +
                ", className='" + className + '\'' +
                ", description='" + description + '\'' +
                ", methods=" + methods +
                ", headerDefinitions=" + headerDefinitions +
                ", commonResponseData=" + commonResponseData +
                '}';
    }
}
