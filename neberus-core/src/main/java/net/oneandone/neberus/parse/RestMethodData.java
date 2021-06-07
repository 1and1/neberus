package net.oneandone.neberus.parse;

import net.oneandone.neberus.model.ApiStatus;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.oneandone.neberus.parse.RestMethodData.ParameterType.BODY;

public class RestMethodData {

    private static final String[] NO_BODY_METHODS = { "GET", "DELETE" };

    public RestClassData containingClass;

    public MethodData methodData;

    public RequestData requestData;

    public List<ParameterInfo> responseValues;

    public List<ResponseData> responseData;

    public RestMethodData(String httpMethod) {
        methodData = new MethodData(httpMethod);
        requestData = new RequestData();
        responseData = new ArrayList<>();
        responseValues = new ArrayList<>();
    }

    /**
     * General method data.
     */
    public static class MethodData {

        public ExecutableElement methodDoc;

        public String httpMethod;

        public String path = "";
        public String label = "";
        public String description = "";
        public String curl;
        public boolean printCurl = false;
        public boolean deprecated = false;
        public String deprecatedDescription;
        public List<ExecutableElement> links = new ArrayList<>();

        public MethodData(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        @Override
        public String toString() {
            return "MethodData{" +
                    "methodDoc=" + methodDoc +
                    ", httpMethod='" + httpMethod + '\'' +
                    ", path='" + path + '\'' +
                    ", label='" + label + '\'' +
                    ", description='" + description + '\'' +
                    ", curl='" + curl + '\'' +
                    ", printCurl=" + printCurl +
                    ", deprecated=" + deprecated +
                    ", deprecatedDescription='" + deprecatedDescription + '\'' +
                    ", links=" + links +
                    '}';
        }
    }

    public void validate(boolean ignoreErrors) {
        boolean invalid = false;

        String methodAndClass = " for method " + methodData.methodDoc.getEnclosingElement() + "." + methodData.methodDoc;

        invalid |= !validateResponses(methodAndClass);
        invalid |= !validateForCurl(methodAndClass);
        invalid |= !validateBodyExistenceForHttpMethod(methodAndClass);
        invalid |= !validateContentTypeWhenBodyExists(methodAndClass);

        if (invalid && !ignoreErrors) {
            throw new IllegalStateException();
        }
    }

    private boolean validateResponses(String methodAndClass) {
        boolean valid = true;

        Map<ApiStatus, List<ResponseData>> groupedResponses = responseData.stream().collect(Collectors.groupingBy(r -> r.status));

        for (Map.Entry<ApiStatus, List<ResponseData>> entry : groupedResponses.entrySet()) {
            if (entry.getValue().size() > 1) {
                valid = false;
                System.err.println("Found multiple responses for the same status <" + entry.getKey() + "> but only one is allowed" + methodAndClass);
            }
        }

        return valid;
    }

    private boolean validateBodyExistenceForHttpMethod(String methodAndClass) {
        boolean valid = true;

        if (Arrays.stream(NO_BODY_METHODS).anyMatch(e -> e.equals(methodData.httpMethod))) {
            if (requestData.mediaType != null && !requestData.mediaType.isEmpty()) {
                System.err.println("Consumes MediaType is not allowed in combination with HttpMethod "
                        + methodData.httpMethod + methodAndClass);
                valid = false;
            }

            if (requestData.parameters.stream().anyMatch(p -> p.parameterType == BODY) || !requestData.entities.isEmpty()) {
                System.err.println("Body parameter is not allowed in combination with HttpMethod "
                        + methodData.httpMethod + methodAndClass);
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateContentTypeWhenBodyExists(String methodAndClass) {
        boolean valid = true;

        if ((requestData.parameters.stream().anyMatch(p -> p.parameterType == BODY) || !requestData.entities.isEmpty())
                && (requestData.mediaType == null || requestData.mediaType.isEmpty())) {
            System.err.println("'Consumes' mediatype is required for method with body parameter" + methodAndClass);
            valid = false;
        }

        return valid;
    }

    private boolean validateForCurl(String methodAndClass) {
        boolean valid = true;

        if (methodData.printCurl && methodData.curl == null
                && Arrays.stream(NO_BODY_METHODS).noneMatch(e -> e.equals(methodData.httpMethod))
                && requestData.parameters.stream().anyMatch(p -> p.parameterType == BODY)
                && (requestData.mediaType == null || requestData.mediaType.isEmpty())) {
            System.err.println("Consumes MediaType is required to generate curl" + methodAndClass);
            valid = false;
        }
        return valid;
    }

    @Override
    public String toString() {
        return "RestMethodData{" +
                "containingClass=" + containingClass.className +
                ", methodData=" + methodData +
                ", requestData=" + requestData +
                ", responseValues=" + responseValues +
                ", responseData=" + responseData +
                '}';
    }

    /**
     * Request related data
     */
    public static class RequestData {

        public List<ParameterInfo> parameters;
        public List<String> mediaType;
        public List<Entity> entities;

        public RequestData() {
            parameters = new ArrayList<>();
            entities = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "RequestData{" +
                    "parameters=" + parameters +
                    ", mediaType=" + mediaType +
                    ", entities=" + entities +
                    '}';
        }
    }

    public enum ParameterType {
        PATH, QUERY, BODY, HEADER, UNSET
    }

    public static class ParameterInfo {

        public String name = "";
        public ParameterType parameterType;
        public TypeMirror entityClass;
        public TypeMirror displayClass;
        public String description = "";
        public List<AllowedValue> allowedValues = new ArrayList<>();
        public List<ParameterInfo> nestedParameters = new ArrayList<>();
        public Boolean optional;
        public boolean deprecated;
        public String deprecatedDescription = "";
        public Map<String, Map<String, String>> constraints = new HashMap<>();

        public ParameterInfo() {
        }

        public boolean isRequired() {
            return optional == null || !optional;
        }

        public void merge(ParameterInfo other) {
            name = other.name.equals("") ? name : other.name;
            parameterType = other.parameterType == null ? parameterType : other.parameterType;
            description = other.description.equals("") ? description : other.description;
            allowedValues = new ArrayList<>(allowedValues);
            allowedValues.addAll(other.allowedValues);
            entityClass = other.entityClass == null ? entityClass : other.entityClass;
            displayClass = other.displayClass == null ? displayClass : other.displayClass;
            optional = other.optional || optional;
            constraints.putAll(other.constraints);
        }

        @Override
        public String toString() {
            return "ParameterInfo{"
                    + "name=" + name
                    + ", parameterType=" + parameterType
                    + ", entityClass=" + entityClass
                    + ", displayClass=" + displayClass
                    + ", description=" + description
                    + ", allowedValues=" + allowedValues
                    + ", nestedParameters=" + nestedParameters
                    + ", optional=" + optional
                    + ", constraints=" + constraints
                    + '}';
        }

    }

    /**
     * Response related data
     */
    public static class ResponseData {

        public ApiStatus status;
        public String description = "";
        public List<HeaderInfo> headers;
        public List<Entity> entities;

        public ResponseData() {
            this.headers = new ArrayList<>();
            this.entities = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "ResponseData{"
                    + "status=" + status + ", "
                    + "description=" + description + ", "
                    + "headers=" + headers
                    + '}';
        }

    }

    public static class Entity {

        public List<Example> examples;
        public String description = "";
        public TypeMirror entityClass;
        public String contentType = "";
        public List<ParameterInfo> nestedParameters;

        public Entity() {
            this.nestedParameters = new ArrayList<>();
            this.examples = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "examples=" + examples +
                    ", description='" + description + '\'' +
                    ", entityClass=" + entityClass +
                    ", contentType='" + contentType + '\'' +
                    ", nestedParameters=" + nestedParameters +
                    '}';
        }
    }

    public static class Example {
        public String title;
        public String description;
        public String value;

        @Override
        public String toString() {
            return "Example{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    public static class HeaderInfo {

        public String name;
        public String description;
        public List<AllowedValue> allowedValues = new ArrayList<>();
        public boolean optional;
        public boolean deprecated;
        public String deprecatedDescription = "";

        @Override
        public String toString() {
            return "HeaderInfo{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", allowedValues=" + allowedValues +
                    ", optional=" + optional +
                    ", deprecated=" + deprecated +
                    ", deprecatedDescription=" + deprecatedDescription +
                    '}';
        }
    }

    public static class AllowedValue {
        public String value;
        public String valueHint;

        public AllowedValue(String value, String valueHint) {
            this.value = value;
            this.valueHint = valueHint;
        }

        @Override
        public String toString() {
            return "AllowedValue{" +
                    "value='" + value + '\'' +
                    ", valueHint='" + valueHint + '\'' +
                    '}';
        }
    }

}
