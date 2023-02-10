package net.oneandone.neberus.parse;

import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.annotation.ApiAllowedValue;
import net.oneandone.neberus.annotation.ApiAllowedValues;
import net.oneandone.neberus.annotation.ApiCurl;
import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiIgnore;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.annotation.ApiOptional;
import net.oneandone.neberus.annotation.ApiParameter;
import net.oneandone.neberus.annotation.ApiParameters;
import net.oneandone.neberus.annotation.ApiRequestEntities;
import net.oneandone.neberus.annotation.ApiRequestEntity;
import net.oneandone.neberus.annotation.ApiRequired;
import net.oneandone.neberus.annotation.ApiResponse;
import net.oneandone.neberus.annotation.ApiResponses;
import net.oneandone.neberus.annotation.ApiType;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.model.FormParameters;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
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
import static net.oneandone.neberus.util.JavaDocUtils.getCommentTextFromInterfaceOrClass;
import static net.oneandone.neberus.util.JavaDocUtils.getCommentTextWithoutInlineTags;
import static net.oneandone.neberus.util.JavaDocUtils.getConstructors;
import static net.oneandone.neberus.util.JavaDocUtils.getDirectAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.getDocCommentTreeFromInterfaceOrClass;
import static net.oneandone.neberus.util.JavaDocUtils.getEnumValuesAsList;
import static net.oneandone.neberus.util.JavaDocUtils.getExtendedCollectionType;
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
@SuppressWarnings("PMD.TooManyStaticImports")
public abstract class MethodParser {

    protected final Options options;

    public static final String VALUE = "value";
    public static final String VALUE_HINT = "valueHint";
    public static final String DETAIL = "detail";
    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";

    public MethodParser(Options options) {
        this.options = options;
    }

    public RestMethodData parseMethod(ExecutableElement method, String httpMethod) {
        try {
            System.out.println(" - " + method);
            RestMethodData data = new RestMethodData(httpMethod);

            addMethodData(method, data);
            addRequestData(method, data);
            addResponseData(method, data);

            data.validate(options.ignoreErrors);

            return data;
        } catch (Exception e) {
            System.out.println("Error parsing method " + method);
            throw e;
        }
    }

    protected void addRequestData(ExecutableElement method, RestMethodData data) {
        addMediaType(method, data);
        addParameters(method, data);
        addCustomParameters(method, data);
        sortByTypeAndOptionalAndDeprecatedState(data.requestData.parameters);
        addCustomRequestEntities(method, data);
    }

    protected boolean skipParameter(ExecutableElement methodDoc, VariableElement parameter, int index) {
        return hasAnnotation(methodDoc, parameter, ApiIgnore.class, index, options.environment);
    }

    protected void addParameters(ExecutableElement method, RestMethodData data) {
        List<? extends VariableElement> parameters = method.getParameters();

        //get the @param tags from the method's javadoc
        Map<String, ParamTree> paramTags = getParamTags(method, options.environment);
        RestMethodData.ParameterInfo formParamContainer = null;

        for (int i = 0; i < parameters.size(); i++) {
            VariableElement parameter = parameters.get(i);

            if (skipParameter(method, parameter, i)) {
                //we don't want to document @Context aso.
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

                // for form params, directly overwrite the entityClass
                if (parameterInfo.displayClass != null) {
                    parameterInfo.entityClass = parameterInfo.displayClass;
                }
                formParamContainer.nestedParameters.add(parameterInfo);

            } else {
                data.requestData.parameters.add(parameterInfo);
            }
        }

        // convert entities
        List<RestMethodData.ParameterInfo> bodyParams = data.requestData.parameters.stream().filter(p -> p.parameterType == BODY).collect(Collectors.toList());

        bodyParams.forEach(p -> {
            data.requestData.parameters.remove(p);

            RestMethodData.Entity entity = new RestMethodData.Entity();
            entity.entityClass = p.entityClass;
            entity.description = p.description;
            entity.nestedParameters = p.nestedParameters;
            data.requestData.entities.add(entity);
        });

        sortByTypeAndOptionalAndDeprecatedState(data.requestData.parameters);

    }

    protected void addCustomRequestEntities(ExecutableElement method, RestMethodData data) {
        //check for the (maybe implicit) container annotation...
        List<AnnotationValue> entityDefinitions = getAnnotationValue(method, ApiRequestEntities.class, VALUE, options.environment);
        if (entityDefinitions != null) {
            //...and iterate over it's content
            entityDefinitions.forEach(repsonse -> addCustomRequestEntity((AnnotationMirror) repsonse.getValue(), data));
        } else {
            //or look for a single annotation
            List<? extends AnnotationMirror> singleEntityDefinition = getAnnotationDesc(method, ApiRequestEntity.class, options.environment);
            singleEntityDefinition.forEach(annotationMirror -> addCustomRequestEntity(annotationMirror, data));
        }
    }

    private void addCustomRequestEntity(AnnotationMirror entityDefinition, RestMethodData data) {
        RestMethodData.Entity entity = new RestMethodData.Entity();
        entity.contentType = extractValue(entityDefinition, "contentType");
        entity.description = extractValue(entityDefinition, DESCRIPTION);
        entity.entityClass = extractValue(entityDefinition, "entityClass");

        if (entity.contentType == null) {
            // if unset, take the first content-type from the method
            entity.contentType = data.requestData.mediaType == null ? null : data.requestData.mediaType.get(0);
        }

        if (entity.description == null) {
            // get javadoc from entityClass, if not set in annotation
            TypeElement element = (TypeElement) options.environment.getTypeUtils().asElement(entity.entityClass);
            if (element != null) {
                entity.description = getCommentTextFromInterfaceOrClass(element, options.environment, false);
            }
        }

        List<AnnotationValue> examples = extractValue(entityDefinition, "examples");
        entity.examples.addAll(getExamples(examples));

        addNestedParameters(entity.entityClass, entity.nestedParameters, new ArrayList<>());

        data.requestData.entities.add(entity);
    }

    protected abstract String getPathParam(ExecutableElement method, VariableElement parameter, int index);

    protected abstract String getQueryParam(ExecutableElement method, VariableElement parameter, int index);

    protected abstract String getHeaderParam(ExecutableElement method, VariableElement parameter, int index);

    protected abstract String getFormParam(ExecutableElement method, VariableElement parameter, int index);

    protected RestMethodData.ParameterInfo parseParameter(ExecutableElement method, VariableElement parameter,
            Map<String, ParamTree> paramTags, int index) {
        try {
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
            addAllowedValuesFromAnnotation(method, parameter, index, parameterInfo);

            return parameterInfo;
        } catch (Exception e) {
            System.out.println("Error parsing parameter " + parameter);
            throw e;
        }
    }

    protected RequiredStatus getRequiredStatus(ExecutableElement method, VariableElement parameter, int index) {
        if (hasAnnotation(method, parameter, ApiRequired.class, index, options.environment)) {
            return RequiredStatus.REQUIRED;
        }
        if (hasAnnotation(method, parameter, ApiOptional.class, index, options.environment)) {
            return RequiredStatus.OPTIONAL;
        }
        return RequiredStatus.UNSET;
    }

    private RequiredStatus getRequiredStatus(ExecutableElement getter) {
        if (hasAnnotation(getter, ApiRequired.class, options.environment)) {
            return RequiredStatus.REQUIRED;
        }
        if (hasAnnotation(getter, ApiOptional.class, options.environment)) {
            return RequiredStatus.OPTIONAL;
        }
        return RequiredStatus.UNSET;
    }

    private RequiredStatus getRequiredStatus(VariableElement param) {
        if (hasDirectAnnotation(param, ApiRequired.class)) {
            return RequiredStatus.REQUIRED;
        }
        if (hasDirectAnnotation(param, ApiOptional.class)) {
            return RequiredStatus.OPTIONAL;
        }
        return RequiredStatus.UNSET;
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
        parameterInfo.required = getRequiredStatus(method, parameter, index);
        parameterInfo.deprecated = hasAnnotation(method, parameter, Deprecated.class, index, options.environment);

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

            sortByTypeAndOptionalAndDeprecatedState(parameterInfo.nestedParameters);
        }
        return parameterInfo;
    }

    protected void addNestedMap(TypeMirror type, List<RestMethodData.ParameterInfo> parentList) {
        List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();

        RestMethodData.ParameterInfo nestedInfoKey = new RestMethodData.ParameterInfo();
        parentList.add(nestedInfoKey);

        nestedInfoKey.name = "[key]";
        nestedInfoKey.parameterType = BODY;
        nestedInfoKey.entityClass = typeArguments.get(0);
        nestedInfoKey.constraints = getConstraints(typeArguments.get(0).getAnnotationMirrors());
        addAllowedValuesFromAnnotation(typeArguments.get(0), nestedInfoKey);

        if (!typeCantBeDocumented(typeArguments.get(0), options)) {
            TypeElement typeElement = (TypeElement) options.environment.getTypeUtils().asElement(typeArguments.get(0));
            nestedInfoKey.description = getCommentTextFromInterfaceOrClass(typeElement, options.environment, false);
            addNestedParameters(typeArguments.get(0), nestedInfoKey.nestedParameters, new ArrayList<>());
        }

        RestMethodData.ParameterInfo nestedInfoValue = new RestMethodData.ParameterInfo();
        parentList.add(nestedInfoValue);

        nestedInfoValue.name = "[value]";
        nestedInfoValue.entityClass = typeArguments.get(1);
        nestedInfoValue.constraints = getConstraints(typeArguments.get(1).getAnnotationMirrors());
        addAllowedValuesFromAnnotation(typeArguments.get(1), nestedInfoValue);

        if (!typeCantBeDocumented(typeArguments.get(1), options)) {
            TypeElement typeElement = (TypeElement) options.environment.getTypeUtils().asElement(typeArguments.get(1));
            nestedInfoValue.description = getCommentTextFromInterfaceOrClass(typeElement, options.environment, false);
            addNestedParameters(typeArguments.get(1), nestedInfoValue.nestedParameters, new ArrayList<>());
        }
    }

    protected void addNestedArray(TypeMirror type, List<RestMethodData.ParameterInfo> parentList) {
        TypeMirror typeArgument = type instanceof ArrayType
                                  ? ((ArrayType) type).getComponentType()
                                  : ((DeclaredType) type).getTypeArguments().get(0);

        RestMethodData.ParameterInfo nestedInfo = new RestMethodData.ParameterInfo();
        nestedInfo.entityClass = typeArgument;
        nestedInfo.constraints = getConstraints(typeArgument.getAnnotationMirrors());
        addAllowedValuesFromAnnotation(typeArgument, nestedInfo);
        parentList.add(nestedInfo);

        nestedInfo.name = "[element]";

        if (!typeCantBeDocumented(typeArgument, options)) {
            TypeElement typeElement = (TypeElement) options.environment.getTypeUtils().asElement(typeArgument);
            nestedInfo.description = getCommentTextFromInterfaceOrClass(typeElement, options.environment, false);
            addNestedParameters(typeArgument, nestedInfo.nestedParameters, new ArrayList<>());
        }

    }

    protected void addNestedParameters(TypeMirror type, List<RestMethodData.ParameterInfo> parentList,
            List<TypeMirror> parentTypes) {
        if (type == null) {
            return;
        }

        try {
            //add nested parameters (ie. fields)

            parentTypes.add(type);

            if (isCollectionType(type)) {
                addNestedArray(type, parentList);
            } else if (isMapType(type)) {
                addNestedMap(type, parentList);
            } else {
                if (typeCantBeDocumented(type, options)) {
                    return;
                }

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
            sortByTypeAndOptionalAndDeprecatedState(parentList);
        }
    }

    private void sortByTypeAndOptionalAndDeprecatedState(List<RestMethodData.ParameterInfo> parentList) {
        parentList.sort((a, b) -> {
            if (a.parameterType != null && b.parameterType != null) {
                int compareType = a.parameterType.compareTo(b.parameterType);

                if (compareType != 0) {
                    return compareType;
                }
            }

            int compareOpt = Integer.compare(a.required.ordinal(), b.required.ordinal());

            if (compareOpt != 0) {
                return compareOpt;
            }

            return Boolean.compare(a.deprecated, b.deprecated);
        });
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
        nestedInfo.required = getRequiredStatus(param);
        nestedInfo.deprecated = hasDirectAnnotation(param, Deprecated.class);

        //add the allowed values, if specified
        addAllowedValuesFromAnnotation(param, nestedInfo);

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
            nestedInfo.description = getCommentTextFromInterfaceOrClass(getter, options.environment, true);
        }

        nestedInfo.allowedValues = getAllowedValuesFromType(getter.getReturnType());
        nestedInfo.entityClass = getter.getReturnType();
        nestedInfo.constraints = getConstraints(getter.getAnnotationMirrors());
        nestedInfo.required = getRequiredStatus(getter);

        nestedInfo.deprecated = hasAnnotation(getter, Deprecated.class, options.environment);

        getterBlockTags.stream()
                .filter(tag -> tag instanceof DeprecatedTree).map(tag -> (DeprecatedTree) tag).findFirst()
                .ifPresent(deprecatedTree -> {
                    nestedInfo.deprecatedDescription = deprecatedTree.getBody().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining());
                });

        getAllowedValuesFromSeeTag(getter, getterBlockTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getAllowedValuesFromLinkTag(getter, getterInlineTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getConstraintsFromSeeTag(getter, getterBlockTags).ifPresent(av -> nestedInfo.constraints = av);
        getConstraintsFromLinkTag(getter, getterInlineTags).ifPresent(av -> nestedInfo.constraints = av);

        if (nestedInfo.allowedValues.isEmpty()) {
            addAllowedValuesFromAnnotation(getter, nestedInfo);
        }

        parentList.add(nestedInfo);

        if (!type.equals(getter.getReturnType()) && !parentTypes.contains(getter.getReturnType())) {
            // break loops
            addNestedParameters(getter.getReturnType(), nestedInfo.nestedParameters, parentTypes); // recursive
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
        nestedInfo.required = getRequiredStatus(field);
        nestedInfo.deprecated = hasDirectAnnotation(field, Deprecated.class);

        getBlockTags(field, options.environment).stream()
                .filter(tag -> tag instanceof DeprecatedTree).map(tag -> (DeprecatedTree) tag).findFirst()
                .ifPresent(deprecatedTree -> {
                    nestedInfo.deprecatedDescription = deprecatedTree.getBody().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining());
                });

        List<? extends DocTree> fieldBlockTags = getBlockTags(field, options.environment);
        List<? extends DocTree> fieldInlineTags = getInlineTags(field, options.environment);

        getAllowedValuesFromSeeTag(field, fieldBlockTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getAllowedValuesFromLinkTag(field, fieldInlineTags).ifPresent(av -> nestedInfo.allowedValues = av);
        getConstraintsFromSeeTag(field, fieldBlockTags).ifPresent(av -> nestedInfo.constraints = av);
        getConstraintsFromLinkTag(field, fieldInlineTags).ifPresent(av -> nestedInfo.constraints = av);

        if (nestedInfo.allowedValues.isEmpty()) {
            addAllowedValuesFromAnnotation(field, nestedInfo);
        }

        parentList.add(nestedInfo);

        if (!type.equals(field.asType()) && !parentTypes.contains(field.asType())) {
            // break loops
            addNestedParameters(field.asType(), nestedInfo.nestedParameters, parentTypes); // recursive
        }
    }

    private void addAllowedValuesFromAnnotation(Element memberDoc, RestMethodData.ParameterInfo nestedInfo) {
        //check for the (maybe implicit) container annotation...
        List<AnnotationValue> parameters = getDirectAnnotationValue(memberDoc, ApiAllowedValues.class, VALUE);
        if (parameters != null) {
            //...and iterate over it's content
            parameters.forEach(repsonse -> addAllowedValuesFromAnnotation((AnnotationMirror) repsonse.getValue(), nestedInfo.allowedValues));
        } else {
            //or look for a single annotation
            List<? extends AnnotationMirror> singleParameter = getAnnotationDesc(memberDoc, ApiAllowedValue.class);
            singleParameter.forEach(annotationMirror -> addAllowedValuesFromAnnotation(annotationMirror, nestedInfo.allowedValues));
        }
    }

    private void addAllowedValuesFromAnnotation(TypeMirror typeArgument, RestMethodData.ParameterInfo nestedInfo) {
        //check for the (maybe implicit) container annotation...
        List<AnnotationValue> parameters = getDirectAnnotationValue(typeArgument, ApiAllowedValues.class, VALUE);
        if (parameters != null) {
            //...and iterate over it's content
            parameters.forEach(repsonse -> addAllowedValuesFromAnnotation((AnnotationMirror) repsonse.getValue(), nestedInfo.allowedValues));
        } else {
            //or look for a single annotation
            List<? extends AnnotationMirror> singleParameter = getAnnotationDesc(typeArgument, ApiAllowedValue.class);
            singleParameter.forEach(annotationMirror -> addAllowedValuesFromAnnotation(annotationMirror, nestedInfo.allowedValues));
        }
    }

    private void addAllowedValuesFromAnnotation(ExecutableElement memberDoc, VariableElement param, int index, RestMethodData.ParameterInfo nestedInfo) {
        //check for the (maybe implicit) container annotation...
        List<AnnotationValue> parameters = getDirectAnnotationValue(param, ApiAllowedValues.class, VALUE);
        if (parameters != null) {
            //...and iterate over it's content
            parameters.forEach(repsonse -> addAllowedValuesFromAnnotation((AnnotationMirror) repsonse.getValue(), nestedInfo.allowedValues));
        } else {
            //or look for a single annotation
            List<? extends AnnotationMirror> singleParameter = getAnnotationDesc(memberDoc, param, ApiAllowedValue.class, index, options.environment);
            singleParameter.forEach(annotationMirror -> addAllowedValuesFromAnnotation(annotationMirror, nestedInfo.allowedValues));
        }
    }

    private void addAllowedValuesFromAnnotation(AnnotationMirror annotationMirror, List<RestMethodData.AllowedValue> allowedValues) {
        TypeMirror enumClass = extractValue(annotationMirror, "enumValues");

        if (enumClass != null && !enumClass.toString().equals(Enum.class.getCanonicalName())) {
            allowedValues.addAll(getAllowedValuesFromType(enumClass));
        } else {
            String value = extractValue(annotationMirror, VALUE);
            String valueHint = extractValue(annotationMirror, VALUE_HINT);

            RestMethodData.AllowedValue allowedValue = new RestMethodData.AllowedValue(value, valueHint);
            allowedValues.add(allowedValue);
        }
    }

    protected List<RestMethodData.AllowedValue> getAllowedValuesFromType(TypeMirror type) {
        if (!isEnum(type, options.environment)) {
            return new ArrayList<>();
        }

        return getEnumValuesAsList(type, options.environment)
                .stream().map(enumValue -> {
                    String valueHint = getCommentText(enumValue, options.environment, true);
                    return new RestMethodData.AllowedValue(enumValue.getSimpleName().toString(), valueHint);
                }).collect(Collectors.toList());
    }

    protected Optional<List<RestMethodData.AllowedValue>> getAllowedValuesFromLinkTag(Element e, List<? extends DocTree> tags) {
        return tags.stream().filter(tag -> LinkTree.class.isAssignableFrom(tag.getClass())).map(tag -> (LinkTree) tag)
                .findFirst().map(linkTree -> {
                    Element referencedElement = getReferencedElement(e, linkTree.getReference(), options.environment);

                    if (referencedElement != null && TypeElement.class.isAssignableFrom(referencedElement.getClass())) {
                        return getAllowedValuesFromType(referencedElement.asType());
                    }

                    return null;
                });
    }

    protected Optional<List<RestMethodData.AllowedValue>> getAllowedValuesFromSeeTag(Element e, List<? extends DocTree> tags) {
        return tags.stream()
                .filter(tag -> SeeTree.class.isAssignableFrom(tag.getClass())).map(tag -> (SeeTree) tag)
                .findFirst().map(seeTree -> {
                    List<RestMethodData.AllowedValue> values = new ArrayList<>();

                    seeTree.getReference().forEach(referenced -> {
                        Element referencedElement = getReferencedElement(e, referenced, options.environment);

                        if (referencedElement != null && TypeElement.class.isAssignableFrom(referencedElement.getClass())) {
                            values.addAll(getAllowedValuesFromType(referencedElement.asType()));
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
            List<? extends AnnotationMirror> singleParameter = getAnnotationDesc(method, ApiParameter.class, options.environment);
            singleParameter.forEach(annotationMirror -> addCustomParameter(annotationMirror, data));
        }
    }

    protected void addCustomParameter(AnnotationMirror parameterDesc, RestMethodData data) {
        RestMethodData.ParameterInfo parameterInfo = parseCustomParameterInfo(parameterDesc);
        List<RestMethodData.ParameterInfo> parameters = data.requestData.parameters;

        addParameterInfo(parameters, parameterInfo);
    }

    protected void addParameterInfo(List<RestMethodData.ParameterInfo> parameters, RestMethodData.ParameterInfo parameterInfo) {
        if (parameterInfo.entityClass == null) {
            parameterInfo.entityClass = options.environment.getElementUtils().getTypeElement("java.lang.String").asType();
        }
        parameters.add(parameterInfo);
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
            for (AnnotationValue allowedValue : allowedValues) {
                AnnotationMirror allowedValueDesc = (AnnotationMirror) allowedValue.getValue();
                addAllowedValuesFromAnnotation(allowedValueDesc, parameterInfo.allowedValues);
            }
        }

        parameterInfo.entityClass = extractValue(parameterDesc, "entityClass");

        Boolean optional = extractValue(parameterDesc, "optional");
        if (optional != null) {
            parameterInfo.required = optional ? RequiredStatus.OPTIONAL : RequiredStatus.REQUIRED;
        }

        Boolean deprecated = extractValue(parameterDesc, "deprecated");
        parameterInfo.deprecated = deprecated != null && deprecated;
        parameterInfo.deprecatedDescription = extractValue(parameterDesc, "deprecatedDescription");

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

                    return (packageName.equals("javax.validation") || packageName.equals("jakarta.validation"))
                           && simpleName.equals("Constraint");
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

            if (data.methodData.path.endsWith("/") && path.startsWith("/")) {
                path = path.replaceFirst("/", "");
            }

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
            DocCommentTree docCommentTree = getDocCommentTreeFromInterfaceOrClass(method, options.environment);

            if (docCommentTree != null) {
                data.methodData.description = docCommentTree.getFullBody().stream()
                        .filter(tag -> tag instanceof TextTree || tag instanceof StartElementTree || tag instanceof EndElementTree || tag instanceof LinkTree)
                        .map(Object::toString)
                        .collect(Collectors.joining());

                docCommentTree.getFullBody().stream().filter(LinkTree.class::isInstance).map(LinkTree.class::cast)
                        .forEach(linkTree -> {
                            Element referencedMember = getReferencedElement(method, linkTree.getReference(), options.environment);
                            if (referencedMember instanceof ExecutableElement) {
                                data.methodData.links.add((ExecutableElement) referencedMember);
                            }
                        });
            } else {
                data.methodData.description = getCommentTextFromInterfaceOrClass(method, options.environment, true);
            }
        }
    }

    protected void addCurl(ExecutableElement methodDoc, RestMethodData data) {
        //look for the ApiCurl annotation on the method
        if (!getAnnotationDesc(methodDoc, ApiCurl.class, options.environment).isEmpty()) {
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
        List<? extends AnnotationMirror> deprecatedAnnotation = getAnnotationDesc(method, Deprecated.class, options.environment);
        if (!deprecatedAnnotation.isEmpty()) {
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
                                data.methodData.links.add((ExecutableElement) referencedMember);
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
        addResponsesFromAnnotations(method, data);
    }

    protected void addResponsesFromAnnotations(ExecutableElement method, RestMethodData data) {
        // the first value from @Produces may be used as Content-Type, if no specific one is defined.
        List<AnnotationValue> produces = getProduces(method);

        //check for the (maybe implicit) container annotation...
        List<AnnotationValue> responses = getAnnotationValue(method, ApiResponses.class, VALUE, options.environment);
        if (responses != null) {
            //...and iterate over it's content
            responses.forEach(repsonse -> {
                addResponse((AnnotationMirror) repsonse.getValue(), data, produces);
            });
        } else {
            //or look for a single annotation
            List<? extends AnnotationMirror> singleResponse = getAnnotationDesc(method, ApiResponse.class, options.environment);
            singleResponse.forEach(annotationMirror -> addResponse(annotationMirror, data, produces));
        }
    }

    protected void addResponse(AnnotationMirror response, RestMethodData data, List<AnnotationValue> produces) {

        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData();
        data.responseData.add(responseData);
        addCommonResponseData(response, produces, responseData);
    }

    protected TypeMirror getResponseEntityClass(ExecutableElement method, AnnotationMirror response) {
        return extractValue(response, "entityClass");
    }

    protected void addCommonResponseData(AnnotationMirror response, List<AnnotationValue> produces, RestMethodData.ResponseData responseData) {
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

                Boolean optional = extractValue(headerDesc, "optional");
                if (optional != null) {
                    headerInfo.required = optional ? RequiredStatus.OPTIONAL : RequiredStatus.REQUIRED;
                }

                Boolean deprecated = extractValue(headerDesc, "deprecated");
                headerInfo.deprecated = deprecated != null && deprecated;
                headerInfo.deprecatedDescription = extractValue(headerDesc, "deprecatedDescription");

                List<AnnotationValue> allowedValues = extractValue(headerDesc, "allowedValues");
                if (allowedValues != null) {
                    for (AnnotationValue allowedValue : allowedValues) {
                        AnnotationMirror allowedValueDesc = (AnnotationMirror) allowedValue.getValue();
                        addAllowedValuesFromAnnotation(allowedValueDesc, headerInfo.allowedValues);
                    }
                }

                responseData.headers.add(headerInfo);
            }
        }

        List<AnnotationValue> entities = extractValue(response, "entities");

        if (entities != null) {
            for (AnnotationValue entity : entities) {
                AnnotationMirror entityDesc = (AnnotationMirror) entity.getValue();

                RestMethodData.Entity responseEntity = new RestMethodData.Entity();
                responseData.entities.add(responseEntity);

                responseEntity.entityClass = extractValue(entityDesc, "entityClass");
                responseEntity.description = extractValue(entityDesc, DESCRIPTION);

                if (responseEntity.entityClass == null) {
                    responseEntity.entityClass = options.environment.getElementUtils().getTypeElement("java.lang.Void").asType();
                }

                if (responseEntity.description == null) {
                    // get javadoc from entityClass, if not set in annotation
                    TypeElement element = (TypeElement) options.environment.getTypeUtils().asElement(responseEntity.entityClass);
                    if (element != null) {
                        responseEntity.description = getCommentTextFromInterfaceOrClass(element, options.environment, false);
                    }
                }

                // lists of entities can't be declared directly in the annotation -> workaround with custom interface that extends List<?>
                TypeMirror extendedCollectionType = getExtendedCollectionType(responseEntity.entityClass, options.environment);
                if (extendedCollectionType != null) {
                    // do this after fetching potential javadoc from the custom interface
                    responseEntity.entityClass = extendedCollectionType;
                }

                String contentTypeFromResponse = extractValue(entityDesc, "contentType");
                if (contentTypeFromResponse != null) {
                    //store the Content-Type defined in the annotation
                    responseEntity.contentType = contentTypeFromResponse;
                } else {
                    //or take the first value from @Produces, if it is available and an entityClass is defined
                    responseEntity.contentType = (String) produces.get(0).getValue();
                }

                addNestedParameters(responseEntity.entityClass, responseEntity.nestedParameters, new ArrayList<>());

                List<AnnotationValue> examples = extractValue(entityDesc, "examples");
                responseEntity.examples.addAll(getExamples(examples));
            }
        }

    }

    private List<RestMethodData.Example> getExamples(List<AnnotationValue> examples) {
        List<RestMethodData.Example> exampleList = new ArrayList<>();

        if (examples != null) {
            for (AnnotationValue example : examples) {
                AnnotationMirror exampleDesc = (AnnotationMirror) example.getValue();
                RestMethodData.Example exampleData = new RestMethodData.Example();
                exampleData.title = extractValue(exampleDesc, TITLE);
                exampleData.value = extractValue(exampleDesc, VALUE);
                exampleData.description = extractValue(exampleDesc, DESCRIPTION);
                exampleList.add(exampleData);
            }
        }
        return exampleList;
    }

}
