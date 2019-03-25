package net.oneandone.neberus.parse;

import java.util.*;

import static net.oneandone.neberus.parse.RestMethodData.ParameterType.BODY;

public class RestUsecaseData {

    public String description;

    public List<UsecaseData> usecases;

    public RestUsecaseData() {
        this.usecases = new ArrayList<>();
    }

    public void validate(boolean ignoreErrors) {
        usecases.forEach(usecase -> {
            usecase.methods.forEach(method -> {
                if (method.linkedMethod == null) {
                    return;
                }

                method.parameters.keySet().forEach(paramKey -> {
                    if (!containsParameter(method.linkedMethod.requestData.parameters, paramKey)) {
                        System.err.println("Parameter defined in usecase <" + usecase.name + "> contains "
                                + "paramter <" + paramKey + "> that is not present in the linked method <" + method.name + ">");
                        if (!ignoreErrors) {
                            throw new IllegalArgumentException();
                        }
                    }
                });

                method.responseValue.keySet().forEach(responseValueKey -> {
                    if (!containsParameter(method.linkedMethod.responseValues, responseValueKey)
                            && !containsResponseValue(method.linkedMethod.responseData, responseValueKey)) {
                        System.err.println("ResponseValue <" + responseValueKey + "> defined in usecase <" + usecase.name + "> "
                                + "is not present in the linked method <" + method.name + ">");
                        if (!ignoreErrors) {
                            throw new IllegalArgumentException();
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
        return responseData.stream().anyMatch(p -> containsParameter(p.nestedParameters, paramKey));
    }

    public static class UsecaseData {

        public String name;
        public String description;

        public List<UsecaseMethodData> methods;

        public UsecaseData() {
            this.methods = new ArrayList<>();
        }
    }

    public static class UsecaseMethodData {

        public RestMethodData linkedMethod;
        public String name;
        public String description;
        public Map<String, UsecaseValueInfo> parameters;
        public Map<String, UsecaseValueInfo> responseValue;

        public UsecaseMethodData() {
            this.parameters = new LinkedHashMap<>();
            this.responseValue = new LinkedHashMap<>();
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
