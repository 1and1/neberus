package net.oneandone.neberus.util;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.annotation.ApiDocumentation;
import net.oneandone.neberus.annotation.ApiIgnore;
import net.oneandone.neberus.model.FormParameters;
import net.oneandone.neberus.parse.RestMethodData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class JavaDocUtils {

    private static final String JSON_IGNORE = "com.fasterxml.jackson.annotation.JsonIgnore";
    private static final String JSON_IGNORE_LEGACY = "org.codehaus.jackson.annotate.JsonIgnore";
    private static final String JSON_PROPERTY = "com.fasterxml.jackson.annotation.JsonProperty";
    private static final String JSON_PROPERTY_LEGACY = "org.codehaus.jackson.annotate.JsonProperty";
    public static final String XML_TRANSIENT = "javax.xml.bind.annotation.XmlTransient";

    private JavaDocUtils() {
    }

    public static Optional<AnnotationDesc> getAnnotationDesc(ClassDoc clazz, Class annotationClass) {
        Optional<ClassDoc> interfaceClass = getInterfaceClass(clazz);
        Optional<AnnotationDesc> onInterface = Optional.empty();

        if (interfaceClass.isPresent()) {
            onInterface = getAnnotationDesc(interfaceClass.get().annotations(), annotationClass);
        }

        return onInterface.isPresent() ? onInterface : getAnnotationDesc(clazz.annotations(), annotationClass);

    }

    public static Optional<AnnotationDesc> getAnnotationDesc(MethodDoc method, Class annotationClass) {
        return getAnnotationDesc(method, annotationClass.getName());
    }

    public static Optional<AnnotationDesc> getAnnotationDesc(MethodDoc method, String annotationClass) {
        Optional<MethodDoc> interfaceMethod = getInterfaceMethod(method);
        Optional<AnnotationDesc> onInterface = Optional.empty();

        if (interfaceMethod.isPresent()) {
            onInterface = getAnnotationDesc(interfaceMethod.get().annotations(), annotationClass);
        }

        return onInterface.isPresent() ? onInterface : getAnnotationDesc(method.annotations(), annotationClass);
    }

    public static Optional<AnnotationDesc> getAnnotationDesc(MethodDoc method, Parameter param, Class annotationClass, int index) {
        return getAnnotationDesc(method, param, annotationClass.getName(), index);
    }

    public static Optional<AnnotationDesc> getAnnotationDesc(MethodDoc method, Parameter param, String annotationClass, int index) {
        Optional<Parameter> interfaceParam = getInterfaceParameter(method, index);
        Optional<AnnotationDesc> onInterface = Optional.empty();

        if (interfaceParam.isPresent()) {
            onInterface = getAnnotationDesc(interfaceParam.get().annotations(), annotationClass);
        }

        return onInterface.isPresent() ? onInterface : getAnnotationDesc(param.annotations(), annotationClass);
    }

    public static Optional<AnnotationDesc> getAnnotationDesc(ProgramElementDoc field, Class annotationClass) {
        return getAnnotationDesc(field, annotationClass.getName());
    }

    public static Optional<AnnotationDesc> getAnnotationDesc(ProgramElementDoc field, String annotationClass) {
        return getAnnotationDesc(field.annotations(), annotationClass);
    }

    public static Optional<AnnotationDesc> getAnnotationDesc(Parameter ctorParam, Class annotationClass) {
        return getAnnotationDesc(ctorParam, annotationClass.getName());
    }

    public static Optional<AnnotationDesc> getAnnotationDesc(Parameter ctorParam, String annotationClass) {
        return getAnnotationDesc(ctorParam.annotations(), annotationClass);
    }

    private static Optional<AnnotationDesc> getAnnotationDesc(AnnotationDesc[] annotations, Class annotationClass) {
        return getAnnotationDesc(annotations, annotationClass.getName());
    }

    private static Optional<AnnotationDesc> getAnnotationDesc(AnnotationDesc[] annotations, String annotationClass) {
        return Stream.of(annotations)
                .filter((AnnotationDesc a) -> a.annotationType().qualifiedTypeName().equals(annotationClass))
                .findFirst();
    }

    public static <T> T getAnnotationValue(ClassDoc clazz, Class annotationClass, String key) {
        Optional<AnnotationDesc> findFirst = getAnnotationDesc(clazz, annotationClass);
        if (!findFirst.isPresent()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(MethodDoc method, Class annotationClass, String key) {
        return getAnnotationValue(method, annotationClass.getName(), key);
    }

    public static <T> T getAnnotationValue(MethodDoc method, String annotationClass, String key) {
        Optional<AnnotationDesc> findFirst = getAnnotationDesc(method, annotationClass);
        if (!findFirst.isPresent()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(ProgramElementDoc field, Class annotationClass, String key) {
        return getAnnotationValue(field, annotationClass.getName(), key);
    }

    public static <T> T getAnnotationValue(ProgramElementDoc field, String annotationClass, String key) {
        Optional<AnnotationDesc> findFirst = getAnnotationDesc(field, annotationClass);
        if (!findFirst.isPresent()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(MethodDoc method, Parameter param, Class annotationClass, int index, String key) {
        return getAnnotationValue(method, param, annotationClass.getName(), index, key);
    }

    public static <T> T getAnnotationValue(MethodDoc method, Parameter param, String annotationClass, int index, String key) {
        Optional<AnnotationDesc> findFirst = getAnnotationDesc(method, param, annotationClass, index);
        if (!findFirst.isPresent()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(Parameter ctorParam, Class annotationClass, String key) {
        return getAnnotationValue(ctorParam, annotationClass.getName(), key);
    }

    public static <T> T getAnnotationValue(Parameter ctorParam, String annotationClass, String key) {
        Optional<AnnotationDesc> findFirst = getAnnotationDesc(ctorParam, annotationClass);
        if (!findFirst.isPresent()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(MethodDoc method, Parameter parameter, Class annotationClass, String key, int index) {
        Optional<AnnotationDesc> findFirst = getAnnotationDesc(method, parameter, annotationClass, index);
        if (!findFirst.isPresent()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static AnnotationDesc[] getAnnotations(MethodDoc method, Parameter parameter, int index) {
        Optional<Parameter> interfaceParam = getInterfaceParameter(method, index);

        ArrayList<AnnotationDesc> annotations = new ArrayList<>(Arrays.asList(parameter.annotations()));

        interfaceParam.ifPresent(value -> annotations.addAll(Arrays.asList(value.annotations())));

        return annotations.toArray(new AnnotationDesc[0]);
    }

    public static boolean hasAnnotation(ProgramElementDoc field, Class annotationClass) {
        return hasAnnotation(field, annotationClass.getName());
    }

    public static boolean hasAnnotation(ProgramElementDoc field, String annotationClass) {
        return hasAnnotation(field.annotations(), annotationClass);
    }

    public static boolean hasAnnotation(Parameter param, Class annotationClass) {
        return hasAnnotation(param, annotationClass.getName());
    }

    public static boolean hasAnnotation(Parameter param, String annotationClass) {
        return hasAnnotation(param.annotations(), annotationClass);
    }

    public static Optional<ClassDoc> getInterfaceClass(ClassDoc clazz) {
        return Stream.of(clazz.interfaces()).filter(iface -> hasAnnotation(iface.annotations(),
                ApiDocumentation.class)).findFirst();
    }

    private static Optional<ClassDoc> getInterfaceClass(MethodDoc method) {
        return getInterfaceClass(method.containingClass());
    }

    private static Optional<MethodDoc> getInterfaceMethod(MethodDoc method) {
        Optional<ClassDoc> interfaceClass = getInterfaceClass(method);

        if (!interfaceClass.isPresent()) {
            return Optional.empty();
        }

        return Stream.of(interfaceClass.get().methods())
                .filter(method::overrides)
                .findFirst();
    }

    private static Optional<Parameter> getInterfaceParameter(MethodDoc method, int index) {
        Optional<MethodDoc> methodInterface = getInterfaceMethod(method);

        if (!methodInterface.isPresent() || methodInterface.get().parameters().length < (index - 1)) {
            return Optional.empty();
        }

        return Optional.of(methodInterface.get().parameters()[index]);
    }

    public static <T> T extractValue(AnnotationDesc findFirst, String key) {
        Optional<AnnotationDesc.ElementValuePair> evPair =
                Stream.of(findFirst.elementValues())
                        .filter((AnnotationDesc.ElementValuePair ev) -> ev.element().name().equals(key)).findFirst();
        if (!evPair.isPresent()) {
            return null;
        }
        Object value = evPair.get().value().value();
        return (T) value;
    }

    public static boolean hasAnnotation(MethodDoc method, Parameter parameter, Class annotationClass, int index) {
        Optional<Parameter> interfaceParam = getInterfaceParameter(method, index);

        boolean definedOnInterface = false;

        if (interfaceParam.isPresent()) {
            definedOnInterface = hasAnnotation(interfaceParam.get().annotations(), annotationClass);
        }

        return definedOnInterface || hasAnnotation(parameter.annotations(), annotationClass);
    }

    public static boolean hasAnnotation(MethodDoc method, Class annotationClass) {
        return hasAnnotation(method, annotationClass.getName());
    }

    public static boolean hasAnnotation(MethodDoc method, String annotationClass) {
        Optional<MethodDoc> interfaceMethod = getInterfaceMethod(method);

        boolean definedOnInterface = false;

        if (interfaceMethod.isPresent()) {
            definedOnInterface = hasAnnotation(interfaceMethod.get().annotations(), annotationClass);
        }

        return definedOnInterface || hasAnnotation(method.annotations(), annotationClass);
    }

    public static boolean hasAnnotation(ClassDoc clazz, Class annotationClass) {
        Optional<ClassDoc> interfaceClass = getInterfaceClass(clazz);

        boolean definedOnInterface = false;

        if (interfaceClass.isPresent()) {
            definedOnInterface = hasAnnotation(interfaceClass.get().annotations(), annotationClass);
        }

        return definedOnInterface || hasAnnotation(clazz.annotations(), annotationClass);
    }

    private static boolean hasAnnotation(AnnotationDesc[] annotations, Class annotationClass) {
        return hasAnnotation(annotations, annotationClass.getName());
    }

    private static boolean hasAnnotation(AnnotationDesc[] annotations, String annotationClass) {
        return Arrays.asList(annotations).stream().anyMatch(a -> a.annotationType().qualifiedTypeName().equals(annotationClass));
    }

    public static String getCommentText(ClassDoc clazz) {
        Optional<ClassDoc> interfaceClass = getInterfaceClass(clazz);

        String onInterface = null;

        if (interfaceClass.isPresent()) {
            onInterface = interfaceClass.get().commentText();
        }

        return onInterface != null ? onInterface : clazz.commentText();
    }

    public static String getCommentText(MethodDoc method) {
        Optional<MethodDoc> interfaceMethod = getInterfaceMethod(method);

        String onInterface = null;

        if (interfaceMethod.isPresent()) {
            onInterface = interfaceMethod.get().commentText();
        }

        return onInterface != null ? onInterface : method.commentText();
    }

    public static ParamTag getParamTag(MethodDoc method, int index, Map<String, ParamTag> paramTags) {
        Optional<MethodDoc> interfaceMethod = getInterfaceMethod(method);

        if (interfaceMethod.isPresent()) { // from interface is preferred, so it can match paramTags precedence
            String name = interfaceMethod.get().parameters()[index].name();
            if (paramTags.containsKey(name)) {
                return paramTags.get(name);
            }
        }

        return paramTags.get(method.parameters()[index].name());
    }

    public static Map<String, ParamTag> getParamTags(MethodDoc method) {
        Optional<MethodDoc> interfaceMethod = getInterfaceMethod(method);

        Map<String, ParamTag> onInterface = new HashMap<>();

        if (interfaceMethod.isPresent()) {
            onInterface = Stream.of(interfaceMethod.get().paramTags())
                    .collect(Collectors.toMap(ParamTag::parameterName, tag -> tag));
        }

        Map<String, ParamTag> onClass = Stream.of(method.paramTags())
                .collect(Collectors.toMap(ParamTag::parameterName, tag -> tag));

        onClass.putAll(onInterface); // onInterface has precedence

        return onClass;
    }

    public static Map<String, ParamTag> getParamTags(ConstructorDoc ctor) {
        return Stream.of(ctor.paramTags())
                .collect(Collectors.toMap(ParamTag::parameterName, tag -> tag));
    }

    public static Tag[] getTags(MethodDoc method) {
        Optional<MethodDoc> interfaceMethod = getInterfaceMethod(method);

        Tag[] onInterface = null;

        if (interfaceMethod.isPresent()) {
            onInterface = interfaceMethod.get().tags();
        }

        return onInterface != null ? onInterface : method.tags();
    }

    public static boolean containedFieldNamesAreNotAvailableOrPackageExcluded(Type fieldType, Options options) {
        return fieldType.asClassDoc() == null
                || options.scanPackages.stream()
                .noneMatch(pack -> fieldType.asClassDoc().containingPackage().name().startsWith(pack))
                || getDataFields(fieldType).keySet().stream()
                .anyMatch((String k) -> k.matches("arg\\d"));
    }

    public static String getPublicFieldName(FieldDoc field) {
        if (hasAnnotation(field, JSON_PROPERTY)) {
            return getAnnotationValue(field, JSON_PROPERTY, "value");
        } else if (hasAnnotation(field, JSON_PROPERTY_LEGACY)) {
            return getAnnotationValue(field, JSON_PROPERTY_LEGACY, "value");
        } else {
            return field.name();
        }
    }

    public static String getPublicParameterName(MethodDoc method, Parameter param, int index) {
        if (hasAnnotation(param, JSON_PROPERTY)) {
            return getAnnotationValue(method, param, JSON_PROPERTY, index, "value");
        } else if (hasAnnotation(param, JSON_PROPERTY_LEGACY)) {
            return getAnnotationValue(method, param, JSON_PROPERTY_LEGACY, index, "value");
        } else {
            return param.name();
        }
    }

    public static String getPublicCtorParmeterName(Parameter param) {
        if (hasAnnotation(param, JSON_PROPERTY)) {
            return getAnnotationValue(param, JSON_PROPERTY, "value");
        } else if (hasAnnotation(param, JSON_PROPERTY_LEGACY)) {
            return getAnnotationValue(param, JSON_PROPERTY_LEGACY, "value");
        } else {
            return param.name();
        }
    }

    public static Map<String, Type> getDataFields(RestMethodData.ParameterInfo param) {

        Map<String, Type> dataFields = new LinkedHashMap<>();

        if (param.entityClass.qualifiedTypeName().equals(FormParameters.class.getCanonicalName())) {
            // manually grouped form parameters
            param.nestedParameters
                    .forEach(np -> dataFields.put(np.name, np.displayClass != null ? np.displayClass : np.entityClass));

        } else {

            dataFields.putAll(getDataFields(param.displayClass != null ? param.displayClass : param.entityClass));
        }

        return dataFields;
    }

    public static Map<String, Type> getDataFields(Type type) {
        if (type.isPrimitive()) {
            return Collections.emptyMap();
        }

        Map<String, Type> dataFields = new LinkedHashMap<>();
        ClassDoc classDoc = type.asClassDoc();

        List<FieldDoc> fields = getVisibleFields(type);

        fields.forEach(field -> {
            Type fieldType = field.type();
            dataFields.put(getPublicFieldName(field), fieldType);
        });

        if (fields.isEmpty()) {
            getGetters(type, dataFields);
        }

        if (classDoc.superclass() != null && !isJavaType(classDoc.superclassType())) {
            dataFields.putAll(getDataFields(classDoc.superclassType()));
        }

        return dataFields;
    }

    private static void getGetters(Type type, Map<String, Type> dataFields) {
        if (type.isPrimitive()) {
            return;
        }

        List<MethodDoc> getters = getVisibleGetters(type);

        if (!isJavaType(type)) {

            getters.forEach(getter -> {
                Type fieldType = getter.returnType();
                dataFields.put(getNameFromGetter(getter), fieldType);
            });
        }

        if (getters.isEmpty()) {
            ConstructorDoc chosenCtor = null;

            for (ConstructorDoc ctor : type.asClassDoc().constructors()) {
                if (chosenCtor == null) {
                    chosenCtor = ctor;
                } else if (getVisibleCtorParameters(ctor).size() > getVisibleCtorParameters(chosenCtor).size()) {
                    chosenCtor = ctor;
                }
            }

            if (chosenCtor != null) {
                for (Parameter param : getVisibleCtorParameters(chosenCtor)) {
                    dataFields.put(getPublicCtorParmeterName(param), param.type());
                }
            }
        }
    }

    public static boolean isArrayType(Type type) {
        return type != null && ("Set".equals(getTypeString(type)) || getTypeString(type).startsWith("Set<") || "List".equals(
                getTypeString(type)) || getTypeString(type).startsWith("List<"));
    }

    public static boolean isMapType(Type type) {
        return type != null && ("Map".equals(getTypeString(type)) || getTypeString(type).startsWith("Map<"));
    }

    public static String getTypeString(Type type) {
        if (type.asClassDoc() != null && type.asClassDoc().isEnum()) {
            FieldDoc[] enumConstants = type.asClassDoc().enumConstants();
            StringJoiner sj = new StringJoiner("|");
            Stream.of(enumConstants).forEach(constant -> sj.add(constant.name()));
            return sj.toString();
        }

        String name = type.simpleTypeName() + type.dimension();

        //printing out "byte[]" as type may confuse people, so change it to String
        if (name.equals("byte[]")) {
            return "String";
        }

        return name;
    }

    public static String getSimpleTypeName(Type type) {

        if (isArrayType(type)) {
            return type.simpleTypeName() + "[" + getSimpleTypeName(type.asParameterizedType().typeArguments()[0]) + "]";
        } else if (isMapType(type)) {
            return type.simpleTypeName() + "["
                    + getSimpleTypeName(type.asParameterizedType().typeArguments()[0])
                    + ", "
                    + getSimpleTypeName(type.asParameterizedType().typeArguments()[1])
                    + "]";
        } else {
            return type.simpleTypeName();
        }

    }

    public static boolean typeCantBeDocumented(Type type, Options options) {
        if (type != null && type.qualifiedTypeName().equals(FormParameters.class.getCanonicalName())) {
            return false;
        }

        return type == null
                || type.asClassDoc() == null
                || type.isPrimitive()
                || type.qualifiedTypeName().startsWith("java.")
                //                || type.asClassDoc().fields(false).length == 0
                || containedFieldNamesAreNotAvailableOrPackageExcluded(type, options);
    }

    public static boolean isJavaType(Type type) {
        return type.qualifiedTypeName().startsWith("java.");
    }

    public static List<FieldDoc> getVisibleFields(Type type) {
        if (type.isPrimitive()) {
            return Collections.emptyList();
        }

        List<FieldDoc> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(type.asClassDoc().fields(true)));

        if (type.asClassDoc().superclass() != null && !isJavaType(type.asClassDoc().superclassType())) {
            fields.addAll(getVisibleFields(type.asClassDoc().superclassType()));
        }
        return fields.stream().filter(JavaDocUtils::fieldIsVisible).collect(Collectors.toList());
    }

    private static boolean fieldIsVisible(FieldDoc field) {
        return !hasAnnotation(field, ApiIgnore.class)
                && !hasAnnotation(field, XML_TRANSIENT)
                && !hasAnnotation(field, JSON_IGNORE)
                && !hasAnnotation(field, JSON_IGNORE_LEGACY)
                && !field.isStatic()
                && !field.isSynthetic()
                && !field.isTransient()
                && field.isPublic();
    }

    public static List<MethodDoc> getVisibleGetters(Type type) {
        if (isJavaType(type) || type.isPrimitive()) {
            return Collections.emptyList();
        }
        List<MethodDoc> methods = new ArrayList<>();
        methods.addAll(Arrays.asList(type.asClassDoc().methods()));

        if (type.asClassDoc().superclass() != null && !isJavaType(type.asClassDoc().superclassType())) {
            methods.addAll(getVisibleGetters(type.asClassDoc().superclassType()));
        }
        return methods.stream().filter(JavaDocUtils::methodIsVisibleGetter).collect(Collectors.toList());
    }

    private static boolean methodIsVisibleGetter(MethodDoc method) {
        return !hasAnnotation(method, ApiIgnore.class)
                && !hasAnnotation(method, XML_TRANSIENT)
                && !hasAnnotation(method, JSON_IGNORE)
                && !hasAnnotation(method, JSON_IGNORE_LEGACY)
                && method.name().startsWith("get")
                && !method.name().equals("get") // ignore get() methods
                && !method.isStatic()
                && !method.isSynthetic()
                && method.isPublic();
    }

    public static List<Parameter> getVisibleCtorParameters(ConstructorDoc chosenCtor) {
        return Arrays.stream(chosenCtor.parameters()).filter(JavaDocUtils::parameterIsVisible).collect(Collectors.toList());
    }

    private static boolean parameterIsVisible(Parameter param) {
        return !hasAnnotation(param, ApiIgnore.class);
    }

    public static String getNameFromGetter(MethodDoc getter) {
        if (hasAnnotation(getter, JSON_PROPERTY)) {
            return getAnnotationValue(getter, JSON_PROPERTY, "value");
        } else if (hasAnnotation(getter, JSON_PROPERTY_LEGACY)) {
            return getAnnotationValue(getter, JSON_PROPERTY_LEGACY, "value");
        } else {
            return getter.name().substring(3, 4).toLowerCase() + getter.name().substring(4);
        }
    }
}
