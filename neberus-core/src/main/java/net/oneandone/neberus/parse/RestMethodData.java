package net.oneandone.neberus.parse;

import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Type;
import net.oneandone.neberus.ResponseType;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.model.ProblemType;

import java.util.ArrayList;
import java.util.List;

public class RestMethodData {

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

        public MethodDoc methodDoc;

        public String httpMethod;

        public String path = "";
        public String label = "";
        public String description = "";
        public String curl;
        public boolean printCurl = false;
        public boolean deprecated = false;
        public String deprecatedDescription;
        public List<MethodDoc> deprecatedLinks = new ArrayList<>();

        public MethodData(String httpMethod) {
            this.httpMethod = httpMethod;
        }

    }

    /**
     * Request related data
     */
    public static class RequestData {

        public List<ParameterInfo> parameters;
        public List<String> mediaType;

        public RequestData() {
            parameters = new ArrayList<>();
        }

    }

    public enum ParameterType {
        PATH, QUERY, BODY, HEADER, UNSET
    }

    public static class ParameterInfo {

        public String name = "";
        public ParameterType parameterType;
        public Type entityClass;
        public Type displayClass;
        public Type containerClass;
        public String description = "";
        public List<String> allowedValues = new ArrayList<>();
        public String allowedValueHint = "";
        public List<ParameterInfo> nestedParameters = new ArrayList<>();
        public boolean optional;

        public ParameterInfo() {
        }

        public void merge(ParameterInfo other) {
            name = other.name.equals("") ? name : other.name;
            parameterType = other.parameterType == null ? parameterType : other.parameterType;
            description = other.description.equals("") ? description : other.description;
            allowedValues = other.allowedValues.equals("") ? allowedValues : other.allowedValues;
            entityClass = other.entityClass == null ? entityClass : other.entityClass;
            displayClass = other.displayClass == null ? displayClass : other.displayClass;
            containerClass = other.containerClass == null ? containerClass : other.containerClass;
            optional = other.optional || optional;
            //TODO merge nestedParameters? those can't be specified in the annotation, so currently there is no need to...
        }

        @Override
        public String toString() {
            return "ParameterInfo{"
                    + "name=" + name
                    + ", parameterType=" + parameterType
                    + ", entityClass=" + entityClass
                    + ", displayClass=" + displayClass
                    + ", containerClass=" + containerClass
                    + ", description=" + description
                    + ", allowedValues=" + allowedValues
                    + ", nestedParameters=" + nestedParameters
                    + ", optional=" + optional
                    + '}';
        }

    }

    /**
     * Response related data
     */
    public static class ResponseData {

        public ResponseData(ResponseType responseType) {
            this.responseType = responseType;
            this.headers = new ArrayList<>();
            this.warnings = new ArrayList<>();
            this.nestedParameters = new ArrayList<>();
        }

        public ApiStatus status;
        public String description = "";
        public ProblemInfo problem;
        public List<ProblemInfo> warnings;
        public Type entityClass;
        public ResponseType responseType;
        public String contentType = "";
        public List<HeaderInfo> headers;
        public List<ParameterInfo> nestedParameters;

        @Override
        public String toString() {
            return "ResponseData{" + "status=" + status + ", description=" + description + ", problem=" + problem + ", warnings=" +
                    warnings + ", entityClass=" + entityClass + ", responseType=" + responseType + ", contentType=" + contentType +
                    ", headers=" + headers + ", nestedParameters=" + nestedParameters + '}';
        }

    }

    public static class ProblemInfo {

        public String title = "...";
        public String detail = "...";
        public ProblemType type;
    }

    public static class HeaderInfo {

        public String name;
        public String description;

    }

}
