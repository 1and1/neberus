package net.oneandone.neberus.parse;

import com.sun.javadoc.*;
import com.sun.tools.javadoc.FieldDocImpl;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.ResponseType;
import net.oneandone.neberus.annotation.*;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.model.FormParameters;
import net.oneandone.neberus.model.ProblemType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.oneandone.neberus.parse.RestMethodData.ParameterType.*;
import static net.oneandone.neberus.util.JavaDocUtils.*;

/**
 * Parses all stuff related to a single REST method.
 */
public abstract class MethodParser {

    protected final Options options;

    public static final String VALUE = "value";
    public static final String DETAIL = "detail";
    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";

    public MethodParser(Options options) {
        this.options = options;
    }

    public RestMethodData parseMethod(MethodDoc method, String httpMethod) {
        RestMethodData data = new RestMethodData(httpMethod);

        addMethodData(method, data);
        addRequestData(method, data);
        addResponseData(method, data);

        return data;
    }

    protected void addRequestData(MethodDoc method, RestMethodData data) {
        addMediaType(method, data);
        addParameters(method, data);
        addCustomParameters(method, data);
    }

    protected abstract boolean skipParameter(MethodDoc methodDoc, Parameter parameter, int index);

    //TODO label for parameters
    protected void addParameters(MethodDoc method, RestMethodData data) {
        Parameter[] parameters = method.parameters();

        //get the @param tags from the method's javadoc
        Map<String, ParamTag> paramTags = getParamTags(method);
        RestMethodData.ParameterInfo formParamContainer = null;

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (skipParameter(method, parameter, i)) {
                //we don't want to document @Context
                continue;
            }

            RestMethodData.ParameterInfo parameterInfo = parseParameter(method, parameter, paramTags, i);

            if (getFormParam(method, parameters[i], i) != null) {
                if (formParamContainer == null) {
                    formParamContainer = new RestMethodData.ParameterInfo();
                    formParamContainer.entityClass = options.rootDoc.classNamed(FormParameters.class.getCanonicalName());
                    formParamContainer.parameterType = BODY;
                    formParamContainer.name = "Form";

                    data.requestData.parameters.add(formParamContainer);
                }
                parameterInfo.containerClass = formParamContainer.entityClass;
                formParamContainer.nestedParameters.add(parameterInfo);

            } else {
                data.requestData.parameters.add(parameterInfo);
            }
        }

        data.requestData.parameters.sort((a, b) -> a.optional && !b.optional ? 1 : a.optional && b.optional ? 0 : -1);

    }

    protected abstract String getPathParam(MethodDoc method, Parameter parameter, int index);

    protected abstract String getQueryParam(MethodDoc method, Parameter parameter, int index);

    protected abstract String getHeaderParam(MethodDoc method, Parameter parameter, int index);

    protected abstract String getFormParam(MethodDoc method, Parameter parameter, int index);

    protected RestMethodData.ParameterInfo parseParameter(MethodDoc method, Parameter parameter, Map<String, ParamTag> paramTags,
                                                          int index) {
        RestMethodData.ParameterInfo parameterInfo = getBasicParameterInfo(method, parameter, index);

        ParamTag paramTag = getParamTag(method, index, paramTags);

        if (paramTag != null) {
            //add the description found in the @param tag
            parameterInfo.description = paramTag.parameterComment();

            getAllowedValuesFromSeeTag(paramTag.inlineTags()).ifPresent(av -> parameterInfo.allowedValues = av);

            //strip inline tags and use only the text, if present
            Stream.of(paramTag.inlineTags()).filter(tag -> tag.name().equals("Text"))
                    .findFirst().ifPresent(textTag -> parameterInfo.description = textTag.text());
        }

        //add the allowed values, if specified
        AnnotationValue[] allowedValues = getAnnotationValue(method, parameter, ApiAllowedValues.class, VALUE, index);
        if (allowedValues != null && allowedValues.length > 0) {
            parameterInfo.allowedValues = Arrays.stream(allowedValues)
                    .map(av -> (String) av.value()).collect(Collectors.toList());
        }

        String allowedValueHint = getAnnotationValue(method, parameter, ApiAllowedValues.class, "valueHint", index);
        if (allowedValueHint != null) {
            parameterInfo.allowedValueHint = allowedValueHint;
        }

        parameterInfo.optional = hasAnnotation(method, parameter, ApiOptional.class, index);

        return parameterInfo;
    }

    private RestMethodData.ParameterInfo getBasicParameterInfo(MethodDoc method, Parameter parameter, int index) {
        RestMethodData.ParameterInfo parameterInfo = new RestMethodData.ParameterInfo();

        //check whether it is a "path", "query" or "body" parameter
        String pathParam = getPathParam(method, parameter, index);
        String queryParam = getQueryParam(method, parameter, index);
        String headerParam = getHeaderParam(method, parameter, index);

        parameterInfo.entityClass = parameter.type();
        parameterInfo.displayClass = getAnnotationValue(method, parameter, ApiType.class, VALUE, index);
        parameterInfo.constraints = getConstraints(getAnnotations(method, parameter, index));

        if (pathParam != null) {
            parameterInfo.name = pathParam;
            parameterInfo.parameterType = PATH;
        } else if (queryParam != null) {
            parameterInfo.name = queryParam;
            parameterInfo.parameterType = QUERY;
        } else if (headerParam != null) {
            parameterInfo.name = headerParam;
            parameterInfo.parameterType = HEADER;
        } else {
            parameterInfo.name = parameter.name();
            parameterInfo.parameterType = BODY;

            addNestedParameters(parameterInfo.displayClass != null ? parameterInfo.displayClass : parameterInfo.entityClass,
                    parameterInfo.nestedParameters, new ArrayList<>());

            parameterInfo.nestedParameters.sort((a, b) -> a.optional && !b.optional ? 1 : a.optional && b.optional ? 0 : -1);
        }
        return parameterInfo;
    }

    protected void addNestedMap(Type type, List<RestMethodData.ParameterInfo> parentList) {
        Type[] typeArguments = type.asParameterizedType().typeArguments();

        RestMethodData.ParameterInfo nestedInfoKey = new RestMethodData.ParameterInfo();
        parentList.add(nestedInfoKey);

        nestedInfoKey.name = "[key]";
        nestedInfoKey.parameterType = BODY;

        if (!typeCantBeDocumented(typeArguments[0], options)) {
            nestedInfoKey.entityClass = typeArguments[0];
            addNestedParameters(typeArguments[0], nestedInfoKey.nestedParameters, new ArrayList<>());
        }

        RestMethodData.ParameterInfo nestedInfoValue = new RestMethodData.ParameterInfo();
        parentList.add(nestedInfoValue);

        nestedInfoValue.name = "[value]";

        if (!typeCantBeDocumented(typeArguments[1], options)) {
            nestedInfoValue.entityClass = typeArguments[1];
            addNestedParameters(typeArguments[1], nestedInfoValue.nestedParameters, new ArrayList<>());
        }
    }

    protected void addNestedArray(Type type, List<RestMethodData.ParameterInfo> parentList) {
        Type[] typeArguments = type.asParameterizedType().typeArguments();

        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();
        parentList.add(nestedInfo);

        nestedInfo.name = "[element]";
        nestedInfo.parameterType = BODY;

        if (!typeCantBeDocumented(typeArguments[0], options)) {
            nestedInfo.entityClass = typeArguments[0];
            addNestedParameters(typeArguments[0], nestedInfo.nestedParameters, new ArrayList<>());
        }

    }

    protected void addNestedParameters(Type type, List<RestMethodData.ParameterInfo> parentList, List<Type> parentTypes) {
        //add nested parameters (ie. fields)
        if (typeCantBeDocumented(type, options)) {
            return;
        }

        parentTypes.add(type);

        if (isArrayType(type)) {
            addNestedArray(type, parentList);
        } else if (isMapType(type)) {
            addNestedMap(type, parentList);
        } else {

            List<FieldDoc> fields = getVisibleFields(type);

            fields.forEach(field -> addNestedField(type, parentList, parentTypes, field));

            if (!fields.isEmpty()) {
                return;
            }

            List<MethodDoc> getters = getVisibleGetters(type);

            getters.forEach(getter -> addNestedGetter(type, parentList, parentTypes, getter));

            if (!getters.isEmpty()) {
                return;
            }

            ConstructorDoc chosenCtor = getCtorDoc(type);

            if (chosenCtor == null) {
                return;
            }

            Map<String, ParamTag> paramTags = getParamTags(chosenCtor);

            for (Parameter param : chosenCtor.parameters()) {
                addNestedCtorParam(type, parentList, parentTypes, paramTags, param);
            }
        }
    }

    private ConstructorDoc getCtorDoc(Type type) {
        ConstructorDoc chosenCtor = null;

        for (ConstructorDoc ctor : type.asClassDoc().constructors()) {
            if (chosenCtor == null) {
                chosenCtor = ctor;
            } else if (ctor.parameters().length > chosenCtor.parameters().length) {
                chosenCtor = ctor;
            }
        }
        return chosenCtor;
    }

    private void addNestedCtorParam(Type type, List<RestMethodData.ParameterInfo> parentList, List<Type> parentTypes, Map<String, ParamTag> paramTags, Parameter param) {
        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();

        ParamTag paramTag = paramTags.get(param.name());

        nestedInfo.name = getPublicCtorParmeterName(param);
        nestedInfo.allowedValues = getAllowedValuesFromType(param.type());
        nestedInfo.entityClass = param.type();
        nestedInfo.constraints = getConstraints(param.annotations());
        nestedInfo.optional = hasAnnotation(param, ApiOptional.class);

        //add the allowed values, if specified
        AnnotationValue[] allowedValues = getAnnotationValue(param, ApiAllowedValues.class, VALUE);
        if (allowedValues != null && allowedValues.length > 0) {
            nestedInfo.allowedValues = Arrays.stream(allowedValues)
                    .map(av -> (String) av.value()).collect(Collectors.toList());
        }

        Type enumClass = getAnnotationValue(param, ApiAllowedValues.class, "enumValues");
        if (enumClass != null) {
            nestedInfo.allowedValues = enumValuesAsList(enumClass.asClassDoc());
        }

        String allowedValueHint = getAnnotationValue(param, ApiAllowedValues.class, "valueHint");
        if (allowedValueHint != null) {
            nestedInfo.allowedValueHint = allowedValueHint;
        }

        if (paramTag != null) {
            nestedInfo.description = paramTag.parameterComment();
            Tag[] inlineTags = paramTag.inlineTags();
            getAllowedValuesFromSeeTag(inlineTags).ifPresent(av -> nestedInfo.allowedValues = av);

            //strip inlinetags and use only the text, if present
            Stream.of(inlineTags).filter(tag -> tag.name().equals("Text"))
                    .findFirst().ifPresent(textTag -> nestedInfo.description = textTag.text());
        }

        parentList.add(nestedInfo);

        if (!type.equals(param.type()) && !parentTypes.contains(param.type())) { // break loops
            addNestedParameters(param.type(), nestedInfo.nestedParameters, parentTypes); // recursive
        }
    }

    private void addNestedGetter(Type type, List<RestMethodData.ParameterInfo> parentList, List<Type> parentTypes, MethodDoc getter) {
        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();
        nestedInfo.name = getNameFromGetter(getter);

        Tag[] inlineTags = getter.inlineTags();

        if (getter.tags("return").length == 1) {
            nestedInfo.description = getter.tags("return")[0].text();
        } else {
            nestedInfo.description = getter.commentText();

            //strip inlinetags and use only the text, if present
            Stream.of(inlineTags).filter(tag -> tag.name().equals("Text"))
                    .findFirst().ifPresent(textTag -> nestedInfo.description = textTag.text());
        }

        nestedInfo.allowedValues = getAllowedValuesFromType(getter.returnType());
        nestedInfo.entityClass = getter.returnType();
        nestedInfo.constraints = getConstraints(getter.annotations());
        nestedInfo.optional = hasAnnotation(getter, ApiOptional.class);

        getAllowedValuesFromSeeTag(inlineTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getAllowedValuesFromSeeTag(getter.tags()).ifPresent(av -> nestedInfo.allowedValues = av);

        if (nestedInfo.allowedValues.isEmpty()) {
            addAllowedValuesFromAnnotation(getter, nestedInfo);
        }

        addAllowedValueHint(getter, nestedInfo);

        parentList.add(nestedInfo);

        if (!type.equals(getter.returnType()) && !parentTypes.contains(getter.returnType())) {
            // break loops
            addNestedParameters(getter.returnType(), nestedInfo.nestedParameters, parentTypes); // recursive
        }
    }

    private void addAllowedValueHint(MemberDoc memberDoc, RestMethodData.ParameterInfo nestedInfo) {
        String allowedValueHint = getAnnotationValue(memberDoc, ApiAllowedValues.class, "valueHint");
        if (allowedValueHint != null) {
            nestedInfo.allowedValueHint = allowedValueHint;
        }
    }

    private void addNestedField(Type type, List<RestMethodData.ParameterInfo> parentList, List<Type> parentTypes, FieldDoc field) {
        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();

        nestedInfo.parameterType = BODY;
        nestedInfo.name = getPublicFieldName(field);
        nestedInfo.description = field.commentText();
        nestedInfo.allowedValues = getAllowedValuesFromType(field.type());
        nestedInfo.entityClass = field.type();
        nestedInfo.constraints = getConstraints(field.annotations());
        nestedInfo.optional = hasAnnotation(field, ApiOptional.class);

        Tag[] inlineTags = field.inlineTags();

        getAllowedValuesFromSeeTag(inlineTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getAllowedValuesFromSeeTag(field.tags()).ifPresent(av -> nestedInfo.allowedValues = av);

        if (nestedInfo.allowedValues.isEmpty()) {
            addAllowedValuesFromAnnotation(field, nestedInfo);
        }

        addAllowedValueHint(field, nestedInfo);


        //strip inlinetags and use only the text, if present
        Stream.of(inlineTags).filter(tag -> tag.name().equals("Text"))
                .findFirst().ifPresent(textTag -> nestedInfo.description = textTag.text());

        parentList.add(nestedInfo);

        if (!type.equals(field.type()) && !parentTypes.contains(field.type())) {
            // break loops
            addNestedParameters(field.type(), nestedInfo.nestedParameters, parentTypes); // recursive
        }
    }

    private void addAllowedValuesFromAnnotation(MemberDoc memberDoc, RestMethodData.ParameterInfo nestedInfo) {
        //add the allowed values, if specified
        AnnotationValue[] allowedValues = getAnnotationValue(memberDoc, ApiAllowedValues.class, VALUE);
        if (allowedValues != null && allowedValues.length > 0) {
            nestedInfo.allowedValues = Arrays.stream(allowedValues)
                    .map(av -> (String) av.value()).collect(Collectors.toList());
        }

        Type enumClass = getAnnotationValue(memberDoc, ApiAllowedValues.class, "enumValues");
        if (enumClass != null) {
            nestedInfo.allowedValues = enumValuesAsList(enumClass.asClassDoc());
        }
    }

    protected List<String> getAllowedValuesFromType(Type type) {
        List<String> allowedValues = Collections.emptyList();

        if (type.asClassDoc() != null && type.asClassDoc().isEnum()) {
            allowedValues = enumValuesAsList(type.asClassDoc());

        }
        return allowedValues;
    }

    protected Optional<List<String>> getAllowedValuesFromSeeTag(Tag[] tags) {
        return Stream.of(tags).filter(tag -> tag instanceof SeeTag).map(tag -> (SeeTag) tag)
                .findFirst().map(seeTag -> {
                    ClassDoc referencedClass = seeTag.referencedClass();

                    if (referencedClass != null && referencedClass.isEnum()) {
                        return enumValuesAsList(referencedClass);
                    }
                    return null;
                });
    }

    protected List<String> enumValuesAsList(ClassDoc enumClassDoc) {
        List<String> list = new ArrayList<>();

        for (FieldDoc enumConstant : enumClassDoc.enumConstants()) {
            list.add(enumConstant.name());
        }

        return list;
    }

    protected void addCustomParameters(MethodDoc method, RestMethodData data) {
        //check for the (maybe implicit) container annotation...
        AnnotationValue[] parameters = getAnnotationValue(method, ApiParameters.class, VALUE);
        if (parameters != null) {
            //...and iterate over it's content
            Stream.of(parameters).forEach(repsonse -> addCustomParameter((AnnotationDesc) repsonse.value(), data));
        } else {
            //or look for a single annotation
            Optional<AnnotationDesc> singleParameter = getAnnotationDesc(method, ApiParameter.class);
            if (singleParameter.isPresent()) {
                addCustomParameter(singleParameter.get(), data);
            }
        }
    }

    protected void addCustomParameter(AnnotationDesc parameterDesc, RestMethodData data) {
        RestMethodData.ParameterInfo parameterInfo = parseCustomParameterInfo(parameterDesc);
        List<RestMethodData.ParameterInfo> parameters = data.requestData.parameters;

        addParameterInfo(parameters, parameterInfo);
    }

    protected void addParameterInfo(List<RestMethodData.ParameterInfo> parameters, RestMethodData.ParameterInfo parameterInfo) {

        parameters.stream().filter(p -> p.entityClass != null && p.entityClass.equals(parameterInfo.containerClass))
                .forEach(p -> {
                    Optional<RestMethodData.ParameterInfo> existingParam = p.nestedParameters.stream()
                            .filter(np -> np.name.equals(parameterInfo.name)).findFirst();

                    if (existingParam.isPresent()) {
                        existingParam.get().merge(parameterInfo);
                    } else {
                        p.nestedParameters.add(parameterInfo);
                    }
                });
    }

    protected void addCustomResponseValues(MethodDoc method, RestMethodData data) {
        //check for the (maybe implicit) container annotation...
        AnnotationValue[] responseValues = getAnnotationValue(method, ApiResponseValues.class, VALUE);
        if (responseValues != null) {
            //...and iterate over it's content
            Stream.of(responseValues).forEach(repsonse -> {
                addCustomResponseValue((AnnotationDesc) repsonse.value(), data);
            });
        } else {
            //or look for a single annotation
            Optional<AnnotationDesc> singleResponseValue = getAnnotationDesc(method, ApiResponseValue.class);
            if (singleResponseValue.isPresent()) {
                addCustomResponseValue(singleResponseValue.get(), data);
            }
        }
    }

    protected void addCustomResponseValue(AnnotationDesc parameterDesc, RestMethodData data) {
        RestMethodData.ParameterInfo parameterInfo = parseCustomParameterInfo(parameterDesc);

        //add param as nested to responseData
        data.responseData.stream().filter(r -> r.entityClass != null && r.entityClass.equals(parameterInfo.containerClass))
                .forEach(r -> {
                    Optional<RestMethodData.ParameterInfo> existingParam = r.nestedParameters.stream()
                            .filter(np -> np.name.equals(parameterInfo.name)).findFirst();

                    if (existingParam.isPresent()) {
                        existingParam.get().merge(parameterInfo);
                    } else {
                        r.nestedParameters.add(parameterInfo);
                    }
                });

        // add unrelated response values
        if (parameterInfo.containerClass == null) {
            data.responseValues.add(parameterInfo);
        }
    }

    protected RestMethodData.ParameterInfo parseCustomParameterInfo(AnnotationDesc parameterDesc) {
        RestMethodData.ParameterInfo parameterInfo = new RestMethodData.ParameterInfo();

        String name = extractValue(parameterDesc, "name");
        if (name != null) {
            parameterInfo.name = name;
        }

        String description = extractValue(parameterDesc, DESCRIPTION);
        if (description != null) {
            parameterInfo.description = description;
        }

        FieldDocImpl type = extractValue(parameterDesc, TYPE);
        if (type != null) {
            parameterInfo.parameterType = valueOf(type.name());
        } else {
            parameterInfo.parameterType = UNSET;
        }


        String[] allowedValues = extractValue(parameterDesc, "allowedValues");
        if (allowedValues != null) {
            parameterInfo.allowedValues = Arrays.asList(allowedValues);
        }

        parameterInfo.containerClass = extractValue(parameterDesc, "containerClass");
        parameterInfo.entityClass = extractValue(parameterDesc, "entityClass");

        Boolean optional = extractValue(parameterDesc, "optional");
        parameterInfo.optional = optional != null && optional;

        return parameterInfo;
    }

    private Map<String, Map<String, String>> getConstraints(AnnotationDesc[] annotations) {
        Map<String, Map<String, String>> constraints = new HashMap<>();

        Arrays.stream(annotations).filter(this::isValidationConstraint).forEach(annotation -> {
            String key = annotation.annotationType().simpleTypeName();

            HashMap<String, String> params = new HashMap<>();

            // add default values
            Arrays.stream(annotation.annotationType().elements()).forEach(element -> {
                if (element.defaultValue() == null) {
                    return;
                }

                Object defaultValue = element.defaultValue().value();

                if (defaultValue instanceof Number) {
                    params.put(element.name(), defaultValue.toString());
                }
            });

            // overwrite with defined values
            Arrays.stream(annotation.elementValues())
                    .forEach(pair -> params.put(pair.element().name(), pair.value().value().toString()));

            constraints.put(key, params);
        });

        return constraints;
    }

    private boolean isValidationConstraint(AnnotationDesc annotation) {
        return Arrays.stream(annotation.annotationType().annotations())
                .anyMatch(a -> a.annotationType().qualifiedName().equals("javax.validation.Constraint"));
    }

    protected void addMethodData(MethodDoc method, RestMethodData data) {
        data.methodData.methodDoc = method;
        addPath(method, data);
        addLabel(method, data);
        addDescription(method, data);
        addCurl(method, data);
        addDeprecated(method, data);
    }

    protected abstract String getRootPath(ClassDoc classDoc);

    protected abstract String getPath(MethodDoc methodDoc);

    protected void addPath(MethodDoc method, RestMethodData data) {
        //start with the root path defined on the class
        String rootPath = getRootPath(method.containingClass());
        if (rootPath != null) {
            data.methodData.path = rootPath;
        }

        //append the method path, if it's there
        String path = getPath(method);
        if (path != null) {
            String divider = data.methodData.path.endsWith("/") || path.startsWith("/") ? "" : "/";
            data.methodData.path += divider + path;
        }
    }

    protected void addDescription(MethodDoc method, RestMethodData data) {
        //look for an ApiDescription annotation on the method...
        String description = getAnnotationValue(method, ApiDescription.class, VALUE);

        if (description != null) {
            //...and use it's value
            data.methodData.description = description;
        } else {
            //or use the javadoc comment instead
            data.methodData.description = getCommentText(method);
        }
    }

    protected void addCurl(MethodDoc methodDoc, RestMethodData data) {
        //look for the ApiCurl annotation on the method
        if (getAnnotationDesc(methodDoc, ApiCurl.class).isPresent()) {
            data.methodData.printCurl = true; //flag the method to display a curl
            String curl = getAnnotationValue(methodDoc, ApiCurl.class, VALUE);
            if (curl != null) {
                //use the provided value as the curl
                data.methodData.curl = curl;
            }
            //or auto generate one later, if no value was set
        }
    }

    protected void addLabel(MethodDoc method, RestMethodData data) {
        //look for an ApiLabel annotation on the method...
        String label = getAnnotationValue(method, ApiLabel.class, VALUE);
        if (label != null) {
            //...and use it's value
            data.methodData.label = label;
        } else {
            //or use the name of the method instead
            data.methodData.label = method.name();
        }
    }

    protected void addDeprecated(MethodDoc method, RestMethodData data) {
        //look for an Deprecated annotation on the method...
        Optional<AnnotationDesc> deprecatedAnnotation = getAnnotationDesc(method, Deprecated.class);
        if (deprecatedAnnotation.isPresent()) {
            data.methodData.deprecated = true;
            //...and look for a description in the javadoc
            Optional<Tag> deprecatedTag = Stream.of(getTags(method)).filter(tag -> tag.name().equals("@deprecated")).findFirst();
            if (deprecatedTag.isPresent()) {
                data.methodData.deprecatedDescription = deprecatedTag.get().text();
                Stream.of(deprecatedTag.get().inlineTags()).filter(tag -> tag instanceof SeeTag).map(tag -> (SeeTag) tag)
                        .forEach(seeTag -> {
                            MemberDoc referencedMember = seeTag.referencedMember();
                            if (referencedMember instanceof MethodDoc) {
                                data.methodData.deprecatedLinks.add((MethodDoc) referencedMember);
                            }
                        });

            }
        }
    }

    protected abstract AnnotationValue[] getConsumes(MethodDoc method);

    protected abstract AnnotationValue[] getProduces(MethodDoc method);

    protected void addMediaType(MethodDoc method, RestMethodData data) {
        AnnotationValue[] consumes = getConsumes(method);
        if (consumes != null) {
            data.requestData.mediaType = Arrays.asList(consumes).stream().map(av -> (String) av.value()).collect(Collectors
                    .toList());
        }
    }

    protected void addResponseData(MethodDoc method, RestMethodData data) {
        addResponsesFromAnnotations(method, data, ApiSuccessResponse.class, ApiSuccessResponses.class, ResponseType.SUCCESS);
        addResponsesFromAnnotations(method, data, ApiWarningResponse.class, ApiWarningResponses.class, ResponseType.WARNING);
        addResponsesFromAnnotations(method, data, ApiProblemResponse.class, ApiProblemResponses.class, ResponseType.PROBLEM);
        addResponsesFromAnnotations(method, data, ApiResponse.class, ApiResponses.class, ResponseType.GENERIC);
        addCustomResponseValues(method, data);
    }

    protected void addResponsesFromAnnotations(MethodDoc method, RestMethodData data, Class responseClass,
                                               Class responseContainerClass, ResponseType responseType) {
        // the first value from @Produces may be used as Content-Type, if no specific one is defined.
        AnnotationValue[] produces = getProduces(method);

        //check for the (maybe implicit) container annotation...
        AnnotationValue[] reponses = getAnnotationValue(method, responseContainerClass, VALUE);
        if (reponses != null) {
            //...and iterate over it's content
            Stream.of(reponses).forEach(repsonse -> {
                addResponse(method, (AnnotationDesc) repsonse.value(), data, produces, responseType);
            });
        } else {
            //or look for a single annotation
            Optional<AnnotationDesc> singleResponse = getAnnotationDesc(method, responseClass);
            if (singleResponse.isPresent()) {
                addResponse(method, singleResponse.get(), data, produces, responseType);
            }
        }
    }

    protected void addResponse(MethodDoc method, AnnotationDesc response, RestMethodData data, AnnotationValue[] produces,
                               ResponseType responseType) {
        switch (responseType) {
            case SUCCESS:
                addSuccessResponse(method, response, data, produces);
                break;
            case WARNING:
                addWarningResponse(method, response, data);
                break;
            case PROBLEM:
                addProblemResponse(method, response, data);
                break;
            case GENERIC:
                addGenericResponse(method, response, data, produces);
                break;
            default:
                break;
        }
    }

    protected void addGenericResponse(MethodDoc method, AnnotationDesc response, RestMethodData data,
                                      AnnotationValue[] produces) {

        FieldDocImpl responseType = extractValue(response, "responseType");
        ResponseType type = ResponseType.valueOf(responseType.name());

        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(type);

        addCommonResponseData(method, response, responseData);

        responseData.entityClass = extractValue(response, "entityClass");

        addResponseData(response, data, produces, responseData);
    }

    protected void addResponseData(AnnotationDesc response, RestMethodData data, AnnotationValue[] produces, RestMethodData.ResponseData responseData) {
        String contentTypeFromResponse = extractValue(response, "contentType");
        if (contentTypeFromResponse != null) {
            //store the Content-Type defined in the annotation
            responseData.contentType = contentTypeFromResponse;
        } else if (produces != null && produces.length > 0 && responseData.entityClass != null) {
            //or take the first value from @Produces, if it is available and an entityClass is defined
            responseData.contentType = (String) produces[0].value();
        }

        if (responseData.entityClass != null) {
            addNestedParameters(responseData.entityClass, responseData.nestedParameters, new ArrayList<>());
        }

        data.responseData.add(responseData);
    }

    protected void addSuccessResponse(MethodDoc method, AnnotationDesc response, RestMethodData data,
                                      AnnotationValue[] produces) {

        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(ResponseType.SUCCESS);

        addCommonResponseData(method, response, responseData);

        responseData.entityClass = extractValue(response, "entityClass");

        addResponseData(response, data, produces, responseData);
    }

    protected void addProblemResponse(MethodDoc method, AnnotationDesc response, RestMethodData data) {
        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(ResponseType.PROBLEM);

        addCommonResponseData(method, response, responseData);

        FieldDoc problemType = extractValue(response, TYPE);

        if (problemType != null) {
            responseData.problem = new RestMethodData.ProblemInfo();
            responseData.problem.type = ProblemType.valueOf(problemType.name());

            String title = extractValue(response, TITLE);
            if (title != null) {
                responseData.problem.title = title;
            }
            String detail = extractValue(response, DETAIL);
            if (detail != null) {
                responseData.problem.detail = detail;
            }
        }

        data.responseData.add(responseData);
    }

    protected void addWarningResponse(MethodDoc method, AnnotationDesc response, RestMethodData data) {
        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(ResponseType.WARNING);

        addCommonResponseData(method, response, responseData);

        AnnotationValue[] warnings = extractValue(response, "warnings");

        if (warnings != null) {
            for (AnnotationValue warning : warnings) {
                AnnotationDesc warningDesc = (AnnotationDesc) warning.value();

                RestMethodData.ProblemInfo warningInfo = new RestMethodData.ProblemInfo();
                warningInfo.type = ProblemType.valueOf(((FieldDoc) extractValue(warningDesc, TYPE)).name());

                String title = extractValue(warningDesc, TITLE);
                if (title != null) {
                    warningInfo.title = title;
                }

                String detail = extractValue(warningDesc, DETAIL);
                if (detail != null) {
                    warningInfo.detail = detail;
                }

                responseData.warnings.add(warningInfo);
            }
        }

        data.responseData.add(responseData);
    }

    protected void addCommonResponseData(MethodDoc method, AnnotationDesc response, RestMethodData.ResponseData responseData) {
        FieldDocImpl status = extractValue(response, "status");
        responseData.status = ApiStatus.valueOf(status.name());

        String description = extractValue(response, DESCRIPTION);
        if (description != null) {
            responseData.description = description;
        }

        AnnotationValue[] headers = extractValue(response, "headers");

        if (headers != null) {
            for (AnnotationValue header : headers) {
                AnnotationDesc headerDesc = (AnnotationDesc) header.value();
                RestMethodData.HeaderInfo headerInfo = new RestMethodData.HeaderInfo();
                headerInfo.name = extractValue(headerDesc, "name");
                headerInfo.description = extractValue(headerDesc, DESCRIPTION);
                responseData.headers.add(headerInfo);
            }
        }
    }

}
