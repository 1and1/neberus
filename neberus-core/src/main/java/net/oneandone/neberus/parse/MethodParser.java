package net.oneandone.neberus.parse;

import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.ResponseType;
import net.oneandone.neberus.annotation.ApiAllowedValues;
import net.oneandone.neberus.annotation.ApiCurl;
import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiIgnore;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.annotation.ApiOptional;
import net.oneandone.neberus.annotation.ApiParameter;
import net.oneandone.neberus.annotation.ApiParameters;
import net.oneandone.neberus.annotation.ApiProblemResponse;
import net.oneandone.neberus.annotation.ApiProblemResponses;
import net.oneandone.neberus.annotation.ApiResponse;
import net.oneandone.neberus.annotation.ApiResponseValue;
import net.oneandone.neberus.annotation.ApiResponseValues;
import net.oneandone.neberus.annotation.ApiResponses;
import net.oneandone.neberus.annotation.ApiSuccessResponse;
import net.oneandone.neberus.annotation.ApiSuccessResponses;
import net.oneandone.neberus.annotation.ApiType;
import net.oneandone.neberus.annotation.ApiWarningResponse;
import net.oneandone.neberus.annotation.ApiWarningResponses;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.model.FormParameters;
import net.oneandone.neberus.model.ProblemType;
import net.oneandone.neberus.util.JavaDocUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.oneandone.neberus.parse.RestMethodData.ParameterType.BODY;
import static net.oneandone.neberus.parse.RestMethodData.ParameterType.HEADER;
import static net.oneandone.neberus.parse.RestMethodData.ParameterType.PATH;
import static net.oneandone.neberus.parse.RestMethodData.ParameterType.QUERY;
import static net.oneandone.neberus.parse.RestMethodData.ParameterType.UNSET;
import static net.oneandone.neberus.parse.RestMethodData.ParameterType.valueOf;
import static net.oneandone.neberus.util.JavaDocUtils.extractValue;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationDesc;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotations;
import static net.oneandone.neberus.util.JavaDocUtils.getBlockTags;
import static net.oneandone.neberus.util.JavaDocUtils.getCommentText;
import static net.oneandone.neberus.util.JavaDocUtils.getCommentTextWithoutInlineTags;
import static net.oneandone.neberus.util.JavaDocUtils.getConstructors;
import static net.oneandone.neberus.util.JavaDocUtils.getDirectAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.getEnumValuesAsList;
import static net.oneandone.neberus.util.JavaDocUtils.getInlineTags;
import static net.oneandone.neberus.util.JavaDocUtils.getNameFromGetter;
import static net.oneandone.neberus.util.JavaDocUtils.getParamTag;
import static net.oneandone.neberus.util.JavaDocUtils.getParamTags;
import static net.oneandone.neberus.util.JavaDocUtils.getParamTreeComment;
import static net.oneandone.neberus.util.JavaDocUtils.getPublicName;
import static net.oneandone.neberus.util.JavaDocUtils.getReferencedElement;
import static net.oneandone.neberus.util.JavaDocUtils.getTags;
import static net.oneandone.neberus.util.JavaDocUtils.getVisibleCtorParameters;
import static net.oneandone.neberus.util.JavaDocUtils.getVisibleFields;
import static net.oneandone.neberus.util.JavaDocUtils.getVisibleGetters;
import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;
import static net.oneandone.neberus.util.JavaDocUtils.hasDirectAnnotation;
import static net.oneandone.neberus.util.JavaDocUtils.isCollectionType;
import static net.oneandone.neberus.util.JavaDocUtils.isEnum;
import static net.oneandone.neberus.util.JavaDocUtils.isMapType;
import static net.oneandone.neberus.util.JavaDocUtils.typeCantBeDocumented;

/**
 * Parses all stuff related to a single REST method.
 */
public abstract class MethodParser {

    protected final Options options;

    public static final String VALUE = "value";
    public static final String VALUE_HINT = "valueHint";
    public static final String DETAIL = "detail";
    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    private static final String[] NO_BODY_METHODS = { "GET", "DELETE" };

    public MethodParser(Options options) {
        this.options = options;
    }

    public RestMethodData parseMethod(ExecutableElement method, String httpMethod) {
        RestMethodData data = new RestMethodData(httpMethod);

        addMethodData(method, data);
        addRequestData(method, data);
        addResponseData(method, data);

        validate(data);

        return data;
    }

    protected void addRequestData(ExecutableElement method, RestMethodData data) {
        addMediaType(method, data);
        addParameters(method, data);
        addCustomParameters(method, data);
    }

    protected boolean skipParameter(ExecutableElement methodDoc, VariableElement parameter, int index) {
        return hasAnnotation(methodDoc, parameter, ApiIgnore.class, index, options.environment);
    }

    //TODO label for parameters
    protected void addParameters(ExecutableElement method, RestMethodData data) {
        List<? extends VariableElement> parameters = method.getParameters();

        //get the @param tags from the method's javadoc
        Map<String, ParamTree> paramTags = getParamTags(method, options.environment);
        RestMethodData.ParameterInfo formParamContainer = null;

        for (int i = 0; i < parameters.size(); i++) {
            VariableElement parameter = parameters.get(i);

            if (skipParameter(method, parameter, i)) {
                //we don't want to document @Context
                continue;
            }

            RestMethodData.ParameterInfo parameterInfo = parseParameter(method, parameter, paramTags, i);

            if (getFormParam(method, parameters.get(i), i) != null) {
                if (formParamContainer == null) {
                    formParamContainer = new RestMethodData.ParameterInfo();
                    formParamContainer.entityClass = options.environment.getElementUtils()
                            .getTypeElement(FormParameters.class.getCanonicalName()).asType();
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

        sortByOptionalState(data.requestData.parameters);

    }

    protected abstract String getPathParam(ExecutableElement method, VariableElement parameter, int index);

    protected abstract String getQueryParam(ExecutableElement method, VariableElement parameter, int index);

    protected abstract String getHeaderParam(ExecutableElement method, VariableElement parameter, int index);

    protected abstract String getFormParam(ExecutableElement method, VariableElement parameter, int index);

    protected RestMethodData.ParameterInfo parseParameter(ExecutableElement method, VariableElement parameter,
                                                          Map<String, ParamTree> paramTags, int index) {
        RestMethodData.ParameterInfo parameterInfo = getBasicParameterInfo(method, parameter, index);

        ParamTree paramTag = getParamTag(method, index, paramTags, options.environment);

        if (paramTag != null) {
            //add the description found in the @param tag
            parameterInfo.description = getParamTreeComment(paramTag);

            List<? extends DocTree> paramBlockTags = getBlockTags(parameter, options.environment);

            getAllowedValuesFromSeeTag(method, paramBlockTags).ifPresent(av -> parameterInfo.allowedValues = av);
            getAllowedValuesFromLinkTag(method, paramTag.getDescription()).ifPresent(av -> parameterInfo.allowedValues = av);
            getConstraintsFromSeeTag(method, paramBlockTags).ifPresent(av -> parameterInfo.constraints = av);
            getConstraintsFromLinkTag(method, paramTag.getDescription()).ifPresent(av -> parameterInfo.constraints = av);
        }

        //add the allowed values, if specified
        List<AnnotationValue> allowedValues = getAnnotationValue(method, parameter, ApiAllowedValues.class, VALUE, index,
                options.environment);

        if (allowedValues != null && !allowedValues.isEmpty()) {
            parameterInfo.allowedValues = allowedValues.stream()
                    .map(av -> (String) av.getValue()).collect(Collectors.toList());
        }

        String allowedValueHint = getAnnotationValue(method, parameter, ApiAllowedValues.class, VALUE_HINT, index,
                options.environment);

        if (allowedValueHint != null) {
            parameterInfo.allowedValueHint = allowedValueHint;
        }

        parameterInfo.optional = isOptional(method, parameter, index);

        return parameterInfo;
    }

    protected boolean isOptional(ExecutableElement method, VariableElement parameter, int index) {
        return hasAnnotation(method, parameter, ApiOptional.class, index, options.environment);
    }

    private RestMethodData.ParameterInfo getBasicParameterInfo(ExecutableElement method, VariableElement parameter, int index) {
        RestMethodData.ParameterInfo parameterInfo = new RestMethodData.ParameterInfo();

        //check whether it is a "path", "query" or "body" parameter
        String pathParam = getPathParam(method, parameter, index);
        String queryParam = getQueryParam(method, parameter, index);
        String headerParam = getHeaderParam(method, parameter, index);

        parameterInfo.entityClass = parameter.asType();
        parameterInfo.displayClass = getAnnotationValue(method, parameter, ApiType.class, VALUE, index, options.environment);
        parameterInfo.constraints = getConstraints(getAnnotations(method, parameter, index, options.environment));

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
            parameterInfo.name = parameter.getSimpleName().toString();
            parameterInfo.parameterType = BODY;

            addNestedParameters(parameterInfo.displayClass != null ? parameterInfo.displayClass : parameterInfo.entityClass,
                    parameterInfo.nestedParameters, new ArrayList<>());

            sortByOptionalState(parameterInfo.nestedParameters);
        }
        return parameterInfo;
    }

    protected void addNestedMap(TypeMirror type, List<RestMethodData.ParameterInfo> parentList) {
        List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();

        RestMethodData.ParameterInfo nestedInfoKey = new RestMethodData.ParameterInfo();
        parentList.add(nestedInfoKey);

        nestedInfoKey.name = "[key]";
        nestedInfoKey.parameterType = BODY;

        if (!typeCantBeDocumented(typeArguments.get(0), options)) {
            nestedInfoKey.entityClass = typeArguments.get(0);
            addNestedParameters(typeArguments.get(0), nestedInfoKey.nestedParameters, new ArrayList<>());
        }

        RestMethodData.ParameterInfo nestedInfoValue = new RestMethodData.ParameterInfo();
        parentList.add(nestedInfoValue);

        nestedInfoValue.name = "[value]";

        if (!typeCantBeDocumented(typeArguments.get(1), options)) {
            nestedInfoValue.entityClass = typeArguments.get(1);
            addNestedParameters(typeArguments.get(1), nestedInfoValue.nestedParameters, new ArrayList<>());
        }
    }

    protected void addNestedArray(TypeMirror type, List<RestMethodData.ParameterInfo> parentList) {
        List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();

        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();
        parentList.add(nestedInfo);

        nestedInfo.name = "[element]";
        nestedInfo.parameterType = BODY;

        if (!typeCantBeDocumented(typeArguments.get(0), options)) {
            nestedInfo.entityClass = typeArguments.get(0);
            addNestedParameters(typeArguments.get(0), nestedInfo.nestedParameters, new ArrayList<>());
        }

    }

    protected void addNestedParameters(TypeMirror type, List<RestMethodData.ParameterInfo> parentList,
                                       List<TypeMirror> parentTypes) {
        try {
            //add nested parameters (ie. fields)
            if (typeCantBeDocumented(type, options)) {
                return;
            }

            parentTypes.add(type);

            if (isCollectionType(type)) {
                addNestedArray(type, parentList);
            } else if (isMapType(type)) {
                addNestedMap(type, parentList);
            } else {

                List<VariableElement> fields = getVisibleFields(type, options.environment);

                fields.forEach(field -> addNestedField(type, parentList, parentTypes, field));

                if (!fields.isEmpty()) {
                    return;
                }

                List<ExecutableElement> getters = getVisibleGetters(type, options.environment);

                getters.forEach(getter -> addNestedGetter(type, parentList, parentTypes, getter));

                if (!getters.isEmpty()) {
                    return;
                }

                ExecutableElement chosenCtor = getCtorDoc(type);

                if (chosenCtor == null) {
                    return;
                }

                Map<String, ParamTree> paramTags = getParamTags(chosenCtor, options.environment);

                for (VariableElement param : getVisibleCtorParameters(chosenCtor)) {
                    addNestedCtorParam(type, parentList, parentTypes, paramTags, param);
                }
            }
        } finally {
            sortByOptionalState(parentList);
        }
    }

    private void sortByOptionalState(List<RestMethodData.ParameterInfo> parentList) {
        parentList.sort((a, b) -> a.optional && !b.optional ? 1 : a.optional && b.optional ? 0 : -1);
    }

    private ExecutableElement getCtorDoc(TypeMirror type) {
        ExecutableElement chosenCtor = null;

        for (ExecutableElement ctor : getConstructors(type, options.environment)) {
            if (chosenCtor == null) {
                chosenCtor = ctor;
            } else if (ctor.getParameters().size() > chosenCtor.getParameters().size()) {
                chosenCtor = ctor;
            }
        }
        return chosenCtor;
    }

    private void addNestedCtorParam(TypeMirror type, List<RestMethodData.ParameterInfo> parentList, List<TypeMirror> parentTypes,
                                    Map<String, ParamTree> paramTags, VariableElement param) {
        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();

        ParamTree paramTag = paramTags.get(param.getSimpleName().toString());

        nestedInfo.name = getPublicName(param);
        nestedInfo.allowedValues = getAllowedValuesFromType(param.asType());
        nestedInfo.entityClass = param.asType();
        nestedInfo.constraints = getConstraints(param.getAnnotationMirrors());
        nestedInfo.optional = hasDirectAnnotation(param, ApiOptional.class);

        //add the allowed values, if specified
        List<AnnotationValue> allowedValues = getDirectAnnotationValue(param, ApiAllowedValues.class, VALUE);
        if (allowedValues != null && !allowedValues.isEmpty()) {
            nestedInfo.allowedValues = allowedValues.stream()
                    .map(av -> (String) av.getValue()).collect(Collectors.toList());
        }

        TypeMirror enumClass = getDirectAnnotationValue(param, ApiAllowedValues.class, "enumValues");
        if (enumClass != null) {
            nestedInfo.allowedValues = getEnumValuesAsList(enumClass, options.environment);
        }

        String allowedValueHint = getDirectAnnotationValue(param, ApiAllowedValues.class, VALUE_HINT);
        if (allowedValueHint != null) {
            nestedInfo.allowedValueHint = allowedValueHint;
        }

        if (paramTag != null) {
            nestedInfo.description = getParamTreeComment(paramTag);
            List<? extends DocTree> paramBlockTags = getBlockTags(param, options.environment);
            getAllowedValuesFromSeeTag(param, paramBlockTags).ifPresent(av -> nestedInfo.allowedValues = av);
            getConstraintsFromLinkTag(param, paramBlockTags).ifPresent(av -> nestedInfo.constraints = av);
        }

        parentList.add(nestedInfo);

        if (!type.equals(param.asType()) && !parentTypes.contains(param.asType())) { // break loops
            addNestedParameters(param.asType(), nestedInfo.nestedParameters, parentTypes); // recursive
        }
    }

    private void addNestedGetter(TypeMirror type, List<RestMethodData.ParameterInfo> parentList, List<TypeMirror> parentTypes,
                                 ExecutableElement getter) {

        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();
        nestedInfo.name = getNameFromGetter(getter, options.environment);

        List<? extends DocTree> getterBlockTags = getBlockTags(getter, options.environment);
        List<? extends DocTree> getterInlineTags = getInlineTags(getter, options.environment);

        Optional<? extends DocTree> returnTag = getterBlockTags.stream()
                .filter(tag -> ReturnTree.class.isAssignableFrom(tag.getClass())).findFirst();

        if (returnTag.isPresent()) {
            nestedInfo.description = getCommentTextWithoutInlineTags(((ReturnTree) returnTag.get()).getDescription());
        } else {
            nestedInfo.description = JavaDocUtils.getCommentTextFromInterfaceOrClass(getter, options.environment, true);
        }

        nestedInfo.allowedValues = getAllowedValuesFromType(getter.getReturnType());
        nestedInfo.entityClass = getter.getReturnType();
        nestedInfo.constraints = getConstraints(getter.getAnnotationMirrors());
        nestedInfo.optional = hasAnnotation(getter, ApiOptional.class, options.environment);

        getAllowedValuesFromSeeTag(getter, getterBlockTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getAllowedValuesFromLinkTag(getter, getterInlineTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getConstraintsFromSeeTag(getter, getterBlockTags).ifPresent(av -> nestedInfo.constraints = av);
        getConstraintsFromLinkTag(getter, getterInlineTags).ifPresent(av -> nestedInfo.constraints = av);

        if (nestedInfo.allowedValues.isEmpty()) {
            addAllowedValuesFromAnnotation(getter, nestedInfo);
        }

        addAllowedValueHint(getter, nestedInfo);

        parentList.add(nestedInfo);

        if (!type.equals(getter.getReturnType()) && !parentTypes.contains(getter.getReturnType())) {
            // break loops
            addNestedParameters(getter.getReturnType(), nestedInfo.nestedParameters, parentTypes); // recursive
        }
    }

    private void addAllowedValueHint(Element memberDoc, RestMethodData.ParameterInfo nestedInfo) {
        String allowedValueHint = getDirectAnnotationValue(memberDoc, ApiAllowedValues.class, VALUE_HINT);
        if (allowedValueHint != null) {
            nestedInfo.allowedValueHint = allowedValueHint;
        }
    }

    private void addNestedField(TypeMirror type, List<RestMethodData.ParameterInfo> parentList, List<TypeMirror> parentTypes,
                                VariableElement field) {
        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();

        nestedInfo.parameterType = BODY;
        nestedInfo.name = getPublicName(field);
        nestedInfo.description = getCommentText(field, options.environment, true);
        nestedInfo.allowedValues = getAllowedValuesFromType(field.asType());
        nestedInfo.entityClass = field.asType();
        nestedInfo.constraints = getConstraints(field.getAnnotationMirrors());
        nestedInfo.optional = hasDirectAnnotation(field, ApiOptional.class);

        List<? extends DocTree> fieldBlockTags = getBlockTags(field, options.environment);
        List<? extends DocTree> fieldInlineTags = getInlineTags(field, options.environment);

        getAllowedValuesFromSeeTag(field, fieldBlockTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getAllowedValuesFromLinkTag(field, fieldInlineTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getConstraintsFromSeeTag(field, fieldBlockTags).ifPresent(av -> nestedInfo.constraints = av);
        getConstraintsFromLinkTag(field, fieldInlineTags).ifPresent(av -> nestedInfo.constraints = av);

        if (nestedInfo.allowedValues.isEmpty()) {
            addAllowedValuesFromAnnotation(field, nestedInfo);
        }

        addAllowedValueHint(field, nestedInfo);
        parentList.add(nestedInfo);

        if (!type.equals(field.asType()) && !parentTypes.contains(field.asType())) {
            // break loops
            addNestedParameters(field.asType(), nestedInfo.nestedParameters, parentTypes); // recursive
        }
    }

    private void addAllowedValuesFromAnnotation(Element memberDoc, RestMethodData.ParameterInfo nestedInfo) {
        //add the allowed values, if specified
        List<AnnotationValue> allowedValues = getDirectAnnotationValue(memberDoc, ApiAllowedValues.class, VALUE);
        if (allowedValues != null && !allowedValues.isEmpty()) {
            nestedInfo.allowedValues = allowedValues.stream()
                    .map(av -> (String) av.getValue()).collect(Collectors.toList());
        }

        TypeMirror enumClass = getDirectAnnotationValue(memberDoc, ApiAllowedValues.class, "enumValues");
        if (enumClass != null) {
            nestedInfo.allowedValues = getEnumValuesAsList(enumClass, options.environment);
        }
    }

    protected List<String> getAllowedValuesFromType(TypeMirror type) {
        List<String> allowedValues = Collections.emptyList();

        if (isEnum(type, options.environment)) {
            allowedValues = getEnumValuesAsList(type, options.environment);
        }

        return allowedValues;
    }

    protected Optional<List<String>> getAllowedValuesFromLinkTag(Element e, List<? extends DocTree> tags) {
        return tags.stream().filter(tag -> LinkTree.class.isAssignableFrom(tag.getClass())).map(tag -> (LinkTree) tag)
                .findFirst().map(linkTree -> {
                    Element referencedElement = getReferencedElement(e, linkTree.getReference(), options.environment);

                    if (referencedElement != null && TypeElement.class.isAssignableFrom(referencedElement.getClass())) {
                        return getEnumValuesAsList((TypeElement) referencedElement, options.environment);
                    }

                    return null;
                });
    }

    protected Optional<List<String>> getAllowedValuesFromSeeTag(Element e, List<? extends DocTree> tags) {
        return tags.stream()
                .filter(tag -> SeeTree.class.isAssignableFrom(tag.getClass())).map(tag -> (SeeTree) tag)
                .findFirst().map(seeTree -> {
                    List<String> values = new ArrayList<>();

                    seeTree.getReference().forEach(referenced -> {
                        Element referencedElement = getReferencedElement(e, referenced, options.environment);

                        if (referencedElement != null && TypeElement.class.isAssignableFrom(referencedElement.getClass())) {
                            values.addAll(getEnumValuesAsList((TypeElement) referencedElement, options.environment));
                        }
                    });

                    return values;
                });
    }

    protected Optional<Map<String, Map<String, String>>> getConstraintsFromLinkTag(Element e, List<? extends DocTree> tags) {
        return tags.stream().filter(tag -> tag instanceof LinkTree).map(tag -> (LinkTree) tag)
                .findFirst().map(linkTree -> {
                    Element referencedElement = getReferencedElement(e, linkTree.getReference(), options.environment);

                    if (referencedElement != null) {
                        return getConstraints(referencedElement.getAnnotationMirrors());
                    }

                    return null;
                });
    }

    protected Optional<Map<String, Map<String, String>>> getConstraintsFromSeeTag(Element e, List<? extends DocTree> tags) {
        return tags.stream()
                .filter(tag -> SeeTree.class.isAssignableFrom(tag.getClass())).map(tag -> (SeeTree) tag)
                .findFirst().map(seeTree -> {
                    Map<String, Map<String, String>> values = new HashMap<>();

                    seeTree.getReference().forEach(referenced -> {
                        Element referencedElement = getReferencedElement(e, referenced, options.environment);

                        if (referencedElement != null) {
                            values.putAll(getConstraints(referencedElement.getAnnotationMirrors()));
                        }
                    });

                    return values;
                });
    }

    protected void addCustomParameters(ExecutableElement method, RestMethodData data) {
        //check for the (maybe implicit) container annotation...
        List<AnnotationValue> parameters = getAnnotationValue(method, ApiParameters.class, VALUE, options.environment);
        if (parameters != null) {
            //...and iterate over it's content
            parameters.forEach(repsonse -> addCustomParameter((AnnotationMirror) repsonse.getValue(), data));
        } else {
            //or look for a single annotation
            Optional<? extends AnnotationMirror> singleParameter = getAnnotationDesc(method, ApiParameter.class,
                    options.environment);

            if (singleParameter.isPresent()) {
                addCustomParameter(singleParameter.get(), data);
            }
        }
    }

    protected void addCustomParameter(AnnotationMirror parameterDesc, RestMethodData data) {
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

    protected void addCustomResponseValues(ExecutableElement method, RestMethodData data) {
        //check for the (maybe implicit) container annotation...
        List<AnnotationValue> responseValues = getAnnotationValue(method, ApiResponseValues.class, VALUE, options.environment);
        if (responseValues != null) {
            //...and iterate over it's content
            responseValues.forEach(repsonse -> {
                addCustomResponseValue((AnnotationMirror) repsonse.getValue(), data);
            });
        } else {
            //or look for a single annotation
            Optional<? extends AnnotationMirror> singleResponseValue = getAnnotationDesc(method, ApiResponseValue.class,
                    options.environment);

            if (singleResponseValue.isPresent()) {
                addCustomResponseValue(singleResponseValue.get(), data);
            }
        }

        sortByOptionalState(data.responseValues);
    }

    protected void addCustomResponseValue(AnnotationMirror parameterDesc, RestMethodData data) {
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

    protected RestMethodData.ParameterInfo parseCustomParameterInfo(AnnotationMirror parameterDesc) {
        RestMethodData.ParameterInfo parameterInfo = new RestMethodData.ParameterInfo();

        String name = extractValue(parameterDesc, "name");
        if (name != null) {
            parameterInfo.name = name;
        }

        String description = extractValue(parameterDesc, DESCRIPTION);
        if (description != null) {
            parameterInfo.description = description;
        }

        VariableElement type = extractValue(parameterDesc, TYPE);
        if (type != null) {
            parameterInfo.parameterType = valueOf(type.getSimpleName().toString());
        } else {
            parameterInfo.parameterType = UNSET;
        }


        List<AnnotationValue> allowedValues = extractValue(parameterDesc, "allowedValues");
        if (allowedValues != null) {
            parameterInfo.allowedValues = allowedValues.stream()
                    .map(av -> (String) av.getValue()).collect(Collectors.toList());
        }

        parameterInfo.containerClass = extractValue(parameterDesc, "containerClass");
        parameterInfo.entityClass = extractValue(parameterDesc, "entityClass");

        Boolean optional = extractValue(parameterDesc, "optional");
        parameterInfo.optional = optional != null && optional;

        return parameterInfo;
    }

    private Map<String, Map<String, String>> getConstraints(List<? extends AnnotationMirror> annotations) {
        Map<String, Map<String, String>> constraints = new HashMap<>();

        annotations.stream().filter(this::isValidationConstraint).forEach(annotation -> {
            String key = annotation.getAnnotationType().asElement().getSimpleName().toString();

            HashMap<String, String> params = new HashMap<>();

            // add default values
            annotation.getAnnotationType().asElement().getEnclosedElements()
                    .stream().filter(e -> e instanceof ExecutableElement)
                    .map(e -> (ExecutableElement) e).forEach(element -> {
                if (element.getDefaultValue() == null) {
                    return;
                }

                Object defaultValue = element.getDefaultValue().getValue();

                if (defaultValue instanceof Number) {
                    params.put(element.getSimpleName().toString(), defaultValue.toString());
                }
            });

            annotation.getElementValues().forEach((element, value) -> {
                    params.put(element.getSimpleName().toString(), value.getValue().toString());
            });

            constraints.put(key, params);
        });

        return constraints;
    }

    private boolean isValidationConstraint(AnnotationMirror annotation) {
        if (annotation.getAnnotationType().asElement() == null) {
            return false;
        }

        return annotation.getAnnotationType().asElement().getAnnotationMirrors().stream()
                .anyMatch(a -> {
                    String packageName = options.environment.getElementUtils()
                            .getPackageOf(a.getAnnotationType().asElement()).getQualifiedName().toString();
                    String simpleName = a.getAnnotationType().asElement().getSimpleName().toString();

                    return packageName.equals("javax.validation") && simpleName.equals("Constraint");
                });
    }

    protected void addMethodData(ExecutableElement method, RestMethodData data) {
        data.methodData.methodDoc = method;
        addPath(method, data);
        addLabel(method, data);
        addDescription(method, data);
        addCurl(method, data);
        addDeprecated(method, data);
    }

    protected abstract String getRootPath(TypeElement classDoc);

    protected abstract String getPath(ExecutableElement methodDoc);

    protected void addPath(ExecutableElement method, RestMethodData data) {
        //start with the root path defined on the class
        String rootPath = getRootPath((TypeElement) method.getEnclosingElement());
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

    protected void addDescription(ExecutableElement method, RestMethodData data) {
        //look for an ApiDescription annotation on the method...
        String description = getAnnotationValue(method, ApiDescription.class, VALUE, options.environment);

        if (description != null) {
            //...and use it's value
            data.methodData.description = description;
        } else {
            //or use the javadoc comment instead
            data.methodData.description = JavaDocUtils.getCommentTextFromInterfaceOrClass(method, options.environment, true);
        }
    }

    protected void addCurl(ExecutableElement methodDoc, RestMethodData data) {
        //look for the ApiCurl annotation on the method
        if (getAnnotationDesc(methodDoc, ApiCurl.class, options.environment).isPresent()) {
            data.methodData.printCurl = true; //flag the method to display a curl
            String curl = getAnnotationValue(methodDoc, ApiCurl.class, VALUE, options.environment);
            if (curl != null) {
                //use the provided value as the curl
                data.methodData.curl = curl;
            }
            //or auto generate one later, if no value was set
        }
    }

    protected void addLabel(ExecutableElement method, RestMethodData data) {
        //look for an ApiLabel annotation on the method...
        String label = getAnnotationValue(method, ApiLabel.class, VALUE, options.environment);
        if (label != null) {
            //...and use it's value
            data.methodData.label = label;
        } else {
            //or use the name of the method instead
            data.methodData.label = method.getSimpleName().toString();
        }
    }

    protected void addDeprecated(ExecutableElement method, RestMethodData data) {
        //look for an Deprecated annotation on the method...
        Optional<? extends AnnotationMirror> deprecatedAnnotation = getAnnotationDesc(method, Deprecated.class, options.environment);
        if (deprecatedAnnotation.isPresent()) {
            data.methodData.deprecated = true;
            //...and look for a description in the javadoc
            Optional<DeprecatedTree> deprecatedTag = getTags(method, options.environment).stream()
                    .filter(tag -> tag instanceof DeprecatedTree).map(tag -> (DeprecatedTree) tag).findFirst();

            if (deprecatedTag.isPresent()) {
                data.methodData.deprecatedDescription = deprecatedTag.get().getBody().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining());

                deprecatedTag.get().getBody().stream().filter(tag -> tag instanceof LinkTree).map(tag -> (LinkTree) tag)
                        .forEach(linkTree -> {
                            Element referencedMember = getReferencedElement(method, linkTree.getReference(), options.environment);
                            if (referencedMember instanceof ExecutableElement) {
                                data.methodData.deprecatedLinks.add((ExecutableElement) referencedMember);
                            }
                        });
            }
        }
    }

    protected abstract List<AnnotationValue> getConsumes(ExecutableElement method);

    protected abstract List<AnnotationValue> getProduces(ExecutableElement method);

    protected void addMediaType(ExecutableElement method, RestMethodData data) {
        List<AnnotationValue> consumes = getConsumes(method);
        if (consumes != null) {
            data.requestData.mediaType = consumes.stream()
                    .map(av -> (String) av.getValue()).collect(Collectors
                            .toList());
        }
    }

    protected void addResponseData(ExecutableElement method, RestMethodData data) {
        addResponsesFromAnnotations(method, data, ApiSuccessResponse.class, ApiSuccessResponses.class, ResponseType.SUCCESS);
        addResponsesFromAnnotations(method, data, ApiWarningResponse.class, ApiWarningResponses.class, ResponseType.WARNING);
        addResponsesFromAnnotations(method, data, ApiProblemResponse.class, ApiProblemResponses.class, ResponseType.PROBLEM);
        addResponsesFromAnnotations(method, data, ApiResponse.class, ApiResponses.class, ResponseType.GENERIC);
        addCustomResponseValues(method, data);
    }

    protected void addResponsesFromAnnotations(ExecutableElement method, RestMethodData data, Class responseClass,
                                               Class responseContainerClass, ResponseType responseType) {
        // the first value from @Produces may be used as Content-Type, if no specific one is defined.
        List<AnnotationValue> produces = getProduces(method);

        //check for the (maybe implicit) container annotation...
        List<AnnotationValue> reponses = getAnnotationValue(method, responseContainerClass, VALUE, options.environment);
        if (reponses != null) {
            //...and iterate over it's content
            reponses.forEach(repsonse -> {
                addResponse(method, (AnnotationMirror) repsonse.getValue(), data, produces, responseType);
            });
        } else {
            //or look for a single annotation
            Optional<? extends AnnotationMirror> singleResponse = getAnnotationDesc(method, responseClass, options.environment);
            if (singleResponse.isPresent()) {
                addResponse(method, singleResponse.get(), data, produces, responseType);
            }
        }
    }

    protected void addResponse(ExecutableElement method, AnnotationMirror response, RestMethodData data, List<AnnotationValue> produces,
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

    protected void addGenericResponse(ExecutableElement method, AnnotationMirror response, RestMethodData data,
                                      List<AnnotationValue> produces) {

        VariableElement responseType = extractValue(response, "responseType");
        ResponseType type = ResponseType.valueOf(responseType.getSimpleName().toString());

        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(type);

        addCommonResponseData(method, response, responseData);

        responseData.entityClass = extractValue(response, "entityClass");

        addResponseData(response, data, produces, responseData);
    }

    protected void addResponseData(AnnotationMirror response, RestMethodData data, List<AnnotationValue> produces,
                                   RestMethodData.ResponseData responseData) {
        String contentTypeFromResponse = extractValue(response, "contentType");
        if (contentTypeFromResponse != null) {
            //store the Content-Type defined in the annotation
            responseData.contentType = contentTypeFromResponse;
        } else if (produces != null && !produces.isEmpty() && responseData.entityClass != null) {
            //or take the first value from @Produces, if it is available and an entityClass is defined
            responseData.contentType = (String) produces.get(0).getValue();
        }

        if (responseData.entityClass != null) {
            addNestedParameters(responseData.entityClass, responseData.nestedParameters, new ArrayList<>());
        }

        data.responseData.add(responseData);
    }

    protected void addSuccessResponse(ExecutableElement method, AnnotationMirror response, RestMethodData data,
                                      List<AnnotationValue> produces) {

        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(ResponseType.SUCCESS);

        addCommonResponseData(method, response, responseData);

        responseData.entityClass = extractValue(response, "entityClass");

        addResponseData(response, data, produces, responseData);
    }

    protected void addProblemResponse(ExecutableElement method, AnnotationMirror response, RestMethodData data) {
        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(ResponseType.PROBLEM);

        addCommonResponseData(method, response, responseData);

        VariableElement problemType = extractValue(response, TYPE);

        if (problemType != null) {
            responseData.problem = new RestMethodData.ProblemInfo();
            responseData.problem.type = ProblemType.valueOf(problemType.getSimpleName().toString());

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

    protected void addWarningResponse(ExecutableElement method, AnnotationMirror response, RestMethodData data) {
        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(ResponseType.WARNING);

        addCommonResponseData(method, response, responseData);

        List<AnnotationValue> warnings = extractValue(response, "warnings");

        if (warnings != null) {
            for (AnnotationValue warning : warnings) {
                AnnotationMirror warningDesc = (AnnotationMirror) warning.getValue();

                RestMethodData.ProblemInfo warningInfo = new RestMethodData.ProblemInfo();
                warningInfo.type = ProblemType.valueOf(((VariableElement) extractValue(warningDesc, TYPE))
                        .getSimpleName().toString());

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

    protected void addCommonResponseData(ExecutableElement method, AnnotationMirror response,
                                         RestMethodData.ResponseData responseData) {
        VariableElement status = extractValue(response, "status");
        responseData.status = ApiStatus.valueOf(status.getSimpleName().toString());

        String description = extractValue(response, DESCRIPTION);
        if (description != null) {
            responseData.description = description;
        }

        List<AnnotationValue> headers = extractValue(response, "headers");

        if (headers != null) {
            for (AnnotationValue header : headers) {
                AnnotationMirror headerDesc = (AnnotationMirror) header.getValue();
                RestMethodData.HeaderInfo headerInfo = new RestMethodData.HeaderInfo();
                headerInfo.name = extractValue(headerDesc, "name");
                headerInfo.description = extractValue(headerDesc, DESCRIPTION);
                responseData.headers.add(headerInfo);
            }
        }
    }

    private void validate(RestMethodData restMethodData) {
        boolean invalid = false;

        String methodAndClass = " for method " + restMethodData.methodData.methodDoc;

        invalid |= !validateForCurl(restMethodData, methodAndClass);
        invalid |= !validateBodyExistence(restMethodData, methodAndClass);

        if (invalid && !options.ignoreErrors) {
            throw new IllegalStateException();
        }
    }

    private boolean validateBodyExistence(RestMethodData restMethodData, String methodAndClass) {
        boolean valid = true;

        if (Arrays.stream(NO_BODY_METHODS).anyMatch(e -> e.equals(restMethodData.methodData.httpMethod))) {
            if (restMethodData.requestData.mediaType != null && !restMethodData.requestData.mediaType.isEmpty()) {
                System.err.println("Consumes MediaType is not allowed in combination with HttpMethod "
                        + restMethodData.methodData.httpMethod + methodAndClass);
                valid = false;
            }

            if (restMethodData.requestData.parameters.stream().anyMatch(p -> p.parameterType == BODY)) {
                System.err.println("Body parameter is not allowed in combination with HttpMethod "
                        + restMethodData.methodData.httpMethod + methodAndClass);
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateForCurl(RestMethodData restMethodData, String methodAndClass) {
        boolean valid = true;

        if (restMethodData.methodData.printCurl && restMethodData.methodData.curl == null
                && Arrays.stream(NO_BODY_METHODS).noneMatch(e -> e.equals(restMethodData.methodData.httpMethod))
                && restMethodData.requestData.parameters.stream().anyMatch(p -> p.parameterType == BODY)
                && (restMethodData.requestData.mediaType == null || restMethodData.requestData.mediaType.isEmpty())) {
            System.err.println("Consumes MediaType is required to generate curl" + methodAndClass);
            valid = false;
        }
        return valid;
    }

}
