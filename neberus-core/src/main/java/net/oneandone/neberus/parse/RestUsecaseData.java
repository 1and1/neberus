package net.oneandone.neberus.parse;

import java.util.*;
import java.util.stream.Collectors;

import static net.oneandone.neberus.parse.RestMethodData.ParameterType.BODY;

public class RestUsecaseData {

    public String description;

    public List<UsecaseData> usecases;

    private static final Set<String> ALLOWED_REST_METHODS = Arrays.stream(new String[]{ "GET", "PUT", "POST", "HEAD", "DELETE", "PATCH" })
            .collect(Collectors.toSet());

    public RestUsecaseData() {
        this.usecases = new ArrayList<>();
    }

    public void validate(boolean ignoreErrors) {
        usecases.forEach(usecase -> {
            usecase.methods.forEach(method -> {

                if (!ALLOWED_REST_METHODS.contains(method.httpMethod)) {
                    System.err.println("Unsupported REST method in usecase: " + method.httpMethod);
                    if (!ignoreErrors) {
                        throw new IllegalStateException();
                    }
                }

                if (method.linkedMethod == null) {
                    return;
                }

                method.parameters.keySet().forEach(paramKey -> {
                    if (!containsParameter(method.linkedMethod.requestData.parameters, paramKey)) {
                        System.err.println("Method defined in usecase <" + usecase.name + "> contains "
                                + "parameter <" + paramKey + "> that is not present in the linked method <" + method.linkedMethod.methodData.label + ">");
                        if (!ignoreErrors) {
                            throw new IllegalStateException();
                        }
                    }
                });

                method.requestBody.keySet().forEach(contentType -> {
                    if (method.linkedMethod.requestData == null
                            || method.linkedMethod.requestData.mediaType == null
                            || !method.linkedMethod.requestData.mediaType.contains(contentType)) {
                        System.err.println("Method defined in usecase <" + usecase.name + "> contains request body with "
                                + "content-type <" + contentType + "> that is not present in the linked method <" + method.linkedMethod.methodData.label + ">");
                        if (!ignoreErrors) {
                            throw new IllegalStateException();
                        }
                    }
                });

                Set<String> linkedResponseContentTypes = method.linkedMethod.responseData.stream()
                        .flatMap(r -> r.entities.stream().map(e -> e.contentType)).collect(Collectors.toSet());

                method.responseBody.keySet().forEach(contentType -> {
                    if (!linkedResponseContentTypes.contains(contentType)) {
                        System.err.println("Method defined in usecase <" + usecase.name + "> contains response body with "
                                + "content-type <" + contentType + "> that is not present in the linked method <" + method.linkedMethod.methodData.label + ">");
                        if (!ignoreErrors) {
                            throw new IllegalStateException();
                        }
                    }
                });

            });
        });
    }

    private boolean containsParameter(List<RestMethodData.ParameterInfo> params, String paramKey) {
        return getParameter(params, paramKey).isPresent();
    }

    public static Optional<RestMethodData.ParameterInfo> getParameter(List<RestMethodData.ParameterInfo> params, String paramKey) {
        String[] split = paramKey.split("\\.", 2);
        String head = split[0];

        Optional<RestMethodData.ParameterInfo> param = params.stream()
                .filter(p -> p.parameterType != BODY && p.name.equals(paramKey) || p.name.equals(head))
                .findFirst();

        if (!param.isPresent()) {
            Optional<RestMethodData.ParameterInfo> body = params.stream().filter(p -> p.parameterType == BODY).findFirst();
            if (body.isPresent()) {
                param = body.get().nestedParameters.stream().filter(p -> p.name.equals(head)).findFirst();
            }

            if (!param.isPresent()) {
                return Optional.empty();
            }
        }

        if (split.length == 1 || param.get().parameterType != BODY) {
            return param;
        }

        String tail = split[1];

        return getParameter(param.get().nestedParameters, tail);
    }

    private boolean containsResponseValue(List<RestMethodData.ResponseData> responseData, String paramKey) {
        return responseData.stream().flatMap(resp -> resp.entities.stream())
                .anyMatch(entity -> containsParameter(entity.nestedParameters, paramKey));
    }

    public static class UsecaseData {

        public String id;
        public String name;
        public String description;

        public List<UsecaseMethodData> methods;

        public UsecaseData(String id) {
            this.id = id;
            this.methods = new ArrayList<>();
        }
    }

    public static class UsecaseMethodData {

        public RestMethodData linkedMethod;
        public String path;
        public String httpMethod;
        public String description;
        public Map<String, UsecaseValueInfo> parameters;
        public Map<String, UsecaseValueInfo> requestBody; // contentType -> valueInfo
        public Map<String, UsecaseValueInfo> responseBody; // contentType -> valueInfo

        public UsecaseMethodData() {
            this.parameters = new LinkedHashMap<>();
            this.requestBody = new LinkedHashMap<>();
            this.responseBody = new LinkedHashMap<>();
        }

    }

    public static class UsecaseValueInfo {
        public String value;
        public String valueHint;

        public UsecaseValueInfo(String value, String valueHint) {
            this.value = value;
            this.valueHint = valueHint;
        }
    }

}
