package net.oneandone.neberus.util;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;
import jdk.javadoc.doclet.DocletEnvironment;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.annotation.ApiDocumentation;
import net.oneandone.neberus.annotation.ApiIgnore;
import net.oneandone.neberus.model.FormParameters;
import net.oneandone.neberus.parse.RestMethodData;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public abstract class JavaDocUtils {

    private static final String JSON_IGNORE = "com.fasterxml.jackson.annotation.JsonIgnore";
    private static final String JSON_IGNORE_LEGACY = "org.codehaus.jackson.annotate.JsonIgnore";
    private static final String JSON_PROPERTY = "com.fasterxml.jackson.annotation.JsonProperty";
    private static final String JSON_PROPERTY_LEGACY = "org.codehaus.jackson.annotate.JsonProperty";
    public static final String XML_TRANSIENT = "javax.xml.bind.annotation.XmlTransient";
    public static final String XML_ROOT_ELEMENT = "javax.xml.bind.annotation.XmlRootElement";
    public static final String JACKSON_XML_ROOT_ELEMENT = "com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement";

    private JavaDocUtils() {
    }

    public static List<? extends AnnotationMirror> getAnnotationDesc(TypeElement clazz, Class annotationClass,
                                                                     DocletEnvironment environment) {

        List<? extends AnnotationMirror> annotationMirrors = collectAllAnnotationMirrors(clazz, environment);
        return getAnnotationDesc(annotationMirrors, annotationClass);
    }

    public static List<? extends AnnotationMirror> getAnnotationDesc(ExecutableElement method, Class annotationClass,
                                                                     DocletEnvironment environment) {
        return getAnnotationDesc(method, annotationClass.getCanonicalName(), environment);
    }

    public static List<? extends AnnotationMirror> getAnnotationDesc(ExecutableElement method, String annotationClass,
                                                                     DocletEnvironment environment) {
        Optional<ExecutableElement> interfaceMethod = getInterfaceMethod(method, environment);
        List<? extends AnnotationMirror> onInterface = Collections.emptyList();

        if (interfaceMethod.isPresent()) {
            onInterface = getDirectAnnotationDesc(interfaceMethod.get().getAnnotationMirrors(), annotationClass);
        }

        return !onInterface.isEmpty() ? onInterface : getDirectAnnotationDesc(method.getAnnotationMirrors(), annotationClass);
    }

    public static List<? extends AnnotationMirror> getAnnotationDesc(ExecutableElement method, VariableElement param,
                                                                     Class annotationClass, int index,
                                                                     DocletEnvironment environment) {
        return getAnnotationDesc(method, param, annotationClass.getCanonicalName(), index, environment);
    }

    public static List<? extends AnnotationMirror> getAnnotationDesc(ExecutableElement method, VariableElement param,
                                                                     String annotationClass, int index,
                                                                     DocletEnvironment environment) {
        Optional<VariableElement> interfaceParam = getInterfaceParameter(method, index, environment);
        List<? extends AnnotationMirror> onInterface = Collections.emptyList();

        if (interfaceParam.isPresent()) {
            onInterface = getDirectAnnotationDesc(interfaceParam.get().getAnnotationMirrors(), annotationClass);
        }

        return !onInterface.isEmpty() ? onInterface : getDirectAnnotationDesc(param.getAnnotationMirrors(), annotationClass);
    }

    public static List<? extends AnnotationMirror> getAnnotationDesc(Element field, Class annotationClass) {
        return getDirectAnnotationDesc(field.getAnnotationMirrors(), annotationClass.getCanonicalName());
    }

    private static List<? extends AnnotationMirror> getAnnotationDesc(Element field, String annotationClass) {
        return getDirectAnnotationDesc(field.getAnnotationMirrors(), annotationClass);
    }

    private static List<? extends AnnotationMirror> getAnnotationDesc(VariableElement ctorParam, String annotationClass) {
        return getDirectAnnotationDesc(ctorParam.getAnnotationMirrors(), annotationClass);
    }

    private static List<? extends AnnotationMirror> getAnnotationDesc(List<? extends AnnotationMirror> annotations,
                                                                      Class annotationClass) {
        return getDirectAnnotationDesc(annotations, annotationClass.getCanonicalName());
    }

    private static List<? extends AnnotationMirror> getDirectAnnotationDesc(List<? extends AnnotationMirror> annotations,
                                                                            String annotationClass) {
        return annotations.stream()
                .filter((AnnotationMirror a) -> ((TypeElement) a.getAnnotationType().asElement())
                        .getQualifiedName().toString().equals(annotationClass))
                .collect(Collectors.toList());
    }

    public static <T> T getAnnotationValue(List<? extends AnnotationMirror> annotationMirrors, String annotationClass, String key) {
        Optional<? extends AnnotationMirror> findFirst = getDirectAnnotationDesc(annotationMirrors, annotationClass).stream().findFirst();
        if (findFirst.isEmpty()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(TypeElement clazz, Class annotationClass, String key, DocletEnvironment environment) {
        Optional<? extends AnnotationMirror> findFirst = getAnnotationDesc(clazz, annotationClass, environment).stream().findFirst();
        if (findFirst.isEmpty()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(ExecutableElement method, Class annotationClass, String key,
                                           DocletEnvironment environment) {
        return getAnnotationValue(method, annotationClass.getCanonicalName(), key, environment);
    }

    public static <T> T getAnnotationValue(ExecutableElement method, String annotationClass, String key,
                                           DocletEnvironment environment) {
        Optional<? extends AnnotationMirror> findFirst = getAnnotationDesc(method, annotationClass, environment).stream().findFirst();
        if (findFirst.isEmpty()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getDirectAnnotationValue(Element field, Class annotationClass, String key) {
        return getDirectAnnotationValue(field, annotationClass.getCanonicalName(), key);
    }

    public static <T> T getDirectAnnotationValue(Element field, String annotationClass, String key) {
        Optional<? extends AnnotationMirror> findFirst = getAnnotationDesc(field, annotationClass).stream().findFirst();
        if (findFirst.isEmpty()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(ExecutableElement method, VariableElement param, Class annotationClass,
                                           int index, String key, DocletEnvironment environment) {
        return getAnnotationValue(method, param, annotationClass.getCanonicalName(), index, key, environment);
    }

    public static <T> T getAnnotationValue(ExecutableElement method, VariableElement param, String annotationClass,
                                           int index, String key, DocletEnvironment environment) {
        Optional<? extends AnnotationMirror> findFirst = getAnnotationDesc(method, param, annotationClass, index, environment).stream().findFirst();
        if (findFirst.isEmpty()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getDirectAnnotationValue(VariableElement ctorParam, Class annotationClass, String key) {
        return getDirectAnnotationValue(ctorParam, annotationClass.getCanonicalName(), key);
    }

    public static <T> T getDirectAnnotationValue(VariableElement ctorParam, String annotationClass, String key) {
        Optional<? extends AnnotationMirror> findFirst = getAnnotationDesc(ctorParam, annotationClass).stream().findFirst();
        if (findFirst.isEmpty()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static <T> T getAnnotationValue(ExecutableElement method, VariableElement parameter,
                                           Class annotationClass, String key, int index, DocletEnvironment environment) {
        Optional<? extends AnnotationMirror> findFirst = getAnnotationDesc(method, parameter, annotationClass,
                index, environment).stream().findFirst();

        if (findFirst.isEmpty()) {
            return null;
        }
        return extractValue(findFirst.get(), key);
    }

    public static List<? extends AnnotationMirror> getAnnotations(ExecutableElement method, VariableElement parameter,
                                                                  int index, DocletEnvironment environment) {
        Optional<VariableElement> interfaceParam = getInterfaceParameter(method, index, environment);

        ArrayList<AnnotationMirror> annotations = new ArrayList<>(parameter.getAnnotationMirrors());

        interfaceParam.ifPresent(value -> annotations.addAll(value.getAnnotationMirrors()));

        return annotations;
    }

    public static boolean hasDirectAnnotation(VariableElement param, Class annotationClass) {
        return hasDirectAnnotation(param, annotationClass.getCanonicalName());
    }

    public static boolean hasDirectAnnotation(VariableElement param, String annotationClass) {
        return hasAnnotation(param.getAnnotationMirrors(), annotationClass);
    }

    public static Optional<TypeElement> getInterfaceClass(TypeElement clazz, DocletEnvironment environment) {

        return clazz.getInterfaces().stream()
                .filter(iface -> !iface.toString().startsWith("java.") && environment.getTypeUtils().asElement(iface) != null)
                .map(iface -> (TypeElement) environment.getTypeUtils().asElement(iface))
                .filter(element -> hasAnnotation(element, ApiDocumentation.class, environment))
                .findFirst();
    }

    private static Optional<TypeElement> getInterfaceClass(ExecutableElement method, DocletEnvironment environment) {
        return getInterfaceClass((TypeElement) method.getEnclosingElement(), environment);
    }

    private static Optional<ExecutableElement> getInterfaceMethod(ExecutableElement method, DocletEnvironment environment) {
        Optional<TypeElement> interfaceClass = getInterfaceClass(method, environment);

        if (interfaceClass.isEmpty()) {
            return Optional.empty();
        }

        return getExecutableElements(interfaceClass.get()).stream()
                .filter(e -> environment.getElementUtils().overrides(method, e, (TypeElement) method.getEnclosingElement()))
                .findFirst();
    }

    private static Optional<VariableElement> getInterfaceParameter(ExecutableElement method, int index,
                                                                   DocletEnvironment environment) {
        Optional<ExecutableElement> methodInterface = getInterfaceMethod(method, environment);

        if (methodInterface.isEmpty() || methodInterface.get().getParameters().size() < (index - 1)) {
            return Optional.empty();
        }

        return Optional.of(methodInterface.get().getParameters().get(index));
    }

    public static <T> T extractValue(AnnotationMirror findFirst, String key) {
        Optional<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> evPair =
                findFirst.getElementValues().entrySet().stream()
                        .filter(ev -> ev.getKey().getSimpleName().toString().equals(key)).findFirst();
        if (evPair.isEmpty()) {
            return null;
        }
        Object value = evPair.get().getValue().getValue();
        return (T) value;
    }

    public static boolean hasAnnotation(ExecutableElement method, VariableElement parameter, Class annotationClass, int index,
                                        DocletEnvironment environment) {
        Optional<VariableElement> interfaceParam = getInterfaceParameter(method, index, environment);

        boolean definedOnInterface = false;

        if (interfaceParam.isPresent()) {
            definedOnInterface = hasAnnotation(interfaceParam.get().getAnnotationMirrors(), annotationClass);
        }

        return definedOnInterface || hasAnnotation(parameter.getAnnotationMirrors(), annotationClass);
    }

    public static boolean hasAnnotation(ExecutableElement method, Class annotationClass, DocletEnvironment environment) {
        return hasAnnotation(method, annotationClass.getCanonicalName(), environment);
    }

    public static boolean hasAnnotation(ExecutableElement method, String annotationClass, DocletEnvironment environment) {
        Optional<ExecutableElement> interfaceMethod = getInterfaceMethod(method, environment);

        boolean definedOnInterface = false;

        if (interfaceMethod.isPresent()) {
            definedOnInterface = hasAnnotation(interfaceMethod.get().getAnnotationMirrors(), annotationClass);
        }

        return definedOnInterface || hasAnnotation(method.getAnnotationMirrors(), annotationClass);
    }

    public static boolean hasAnnotation(TypeElement clazz, Class annotationClass, DocletEnvironment environment) {

        List<? extends AnnotationMirror> allAnnotationMirrors = collectAllAnnotationMirrors(clazz, environment);

        return hasAnnotation(allAnnotationMirrors, annotationClass);
    }

    private static List<? extends AnnotationMirror> collectAllAnnotationMirrors(TypeElement clazz, DocletEnvironment environment) {
        List<AnnotationMirror> allAnnotationMirrors = new ArrayList<>();

        if (clazz == null) {
            return allAnnotationMirrors;
        }

        allAnnotationMirrors.addAll(clazz.getAnnotationMirrors());

        clazz.getInterfaces().forEach(iface -> allAnnotationMirrors
                .addAll(collectAllAnnotationMirrors((TypeElement) environment.getTypeUtils().asElement(iface), environment)));

        Optional.ofNullable(clazz.getSuperclass()).ifPresent(superClass -> allAnnotationMirrors
                .addAll(collectAllAnnotationMirrors((TypeElement) environment.getTypeUtils().asElement(superClass), environment)));

        return allAnnotationMirrors;
    }

    private static boolean hasAnnotation(List<? extends AnnotationMirror> annotations, Class annotationClass) {
        return hasAnnotation(annotations, annotationClass.getCanonicalName());
    }

    private static boolean hasAnnotation(List<? extends AnnotationMirror> annotations, String annotationClass) {
        return annotations.stream().anyMatch(a -> ((TypeElement) a.getAnnotationType().asElement())
                .getQualifiedName().toString().equals(annotationClass));
    }

    public static String getCommentTextFromInterfaceOrClass(TypeElement clazz, DocletEnvironment environment,
                                                            boolean stripInlineTags) {
        Optional<TypeElement> interfaceClass = getInterfaceClass(clazz, environment);

        String onInterface = null;

        if (interfaceClass.isPresent()) {
            onInterface = getCommentText(interfaceClass.get(), environment, stripInlineTags);
        }

        return onInterface != null ? onInterface : getCommentText(clazz, environment, stripInlineTags);
    }

    public static String getCommentTextFromInterfaceOrClass(ExecutableElement method, DocletEnvironment environment,
                                                            boolean stripInlineTags) {
        Optional<ExecutableElement> interfaceMethod = getInterfaceMethod(method, environment);

        String onInterface = null;

        if (interfaceMethod.isPresent()) {
            onInterface = getCommentText(interfaceMethod.get(), environment, stripInlineTags);
        }

        return onInterface != null
               ? onInterface
               : getCommentText(method, environment, stripInlineTags);
    }

    public static ParamTree getParamTag(ExecutableElement method, int index, Map<String, ParamTree> paramTags,
                                        DocletEnvironment environment) {
        Optional<ExecutableElement> interfaceMethod = getInterfaceMethod(method, environment);

        if (interfaceMethod.isPresent()) { // from interface is preferred, so it can match paramTags precedence
            String name = interfaceMethod.get().getParameters().get(index).getSimpleName().toString();
            if (paramTags.containsKey(name)) {
                return paramTags.get(name);
            }
        }

        return paramTags.get(method.getParameters().get(index).getSimpleName().toString());
    }

    public static Map<String, ParamTree> getParamTags(ExecutableElement method, DocletEnvironment environment) {
        Optional<ExecutableElement> interfaceMethod = getInterfaceMethod(method, environment);

        Map<String, ParamTree> onInterface = new HashMap<>();

        if (interfaceMethod.isPresent()) {
            onInterface = getBlockTags(interfaceMethod.get(), environment).stream()
                    .filter(d -> d instanceof ParamTree)
                    .map(d -> (ParamTree) d)
                    .collect(Collectors.toMap(p -> p.getName().getName().toString(), p -> p));
        }

        Map<String, ParamTree> onClass = getBlockTags(method, environment).stream()
                .filter(d -> d instanceof ParamTree)
                .map(d -> (ParamTree) d)
                .collect(Collectors.toMap(p -> p.getName().getName().toString(), p -> p));

        onClass.putAll(onInterface); // onInterface has precedence

        return onClass;
    }

    public static String getParamTreeComment(ParamTree param) {
        List<? extends DocTree> description = param.getDescription();

        if (description.isEmpty()) {
            return "";
        }

        return getCommentTextWithoutInlineTags(description);
    }

    public static List<? extends DocTree> getTags(ExecutableElement method, DocletEnvironment environment) {
        Optional<ExecutableElement> interfaceMethod = getInterfaceMethod(method, environment);

        List<? extends DocTree> onInterface = null;

        if (interfaceMethod.isPresent()) {
            onInterface = environment.getDocTrees().getDocCommentTree(interfaceMethod.get()).getBlockTags();
        }

        return onInterface != null ? onInterface : environment.getDocTrees().getDocCommentTree(method).getBlockTags();
    }

    public static boolean containedFieldNamesAreNotAvailableOrPackageExcluded(TypeMirror fieldType, Options options) {
        var element = options.environment.getTypeUtils().asElement(fieldType);

        return element == null
                || options.scanPackages.stream()
                .noneMatch(pack -> getPackageName(element, options.environment).startsWith(pack))
                || getDataFields(fieldType, options.environment).isEmpty()
                || getDataFields(fieldType, options.environment).keySet().stream()
                .anyMatch((String k) -> k.matches("arg\\d"));
    }

    public static String getPublicName(VariableElement param) {
        if (hasDirectAnnotation(param, JSON_PROPERTY)) {
            return getDirectAnnotationValue(param, JSON_PROPERTY, "value");
        } else if (hasDirectAnnotation(param, JSON_PROPERTY_LEGACY)) {
            return getDirectAnnotationValue(param, JSON_PROPERTY_LEGACY, "value");
        } else {
            return param.getSimpleName().toString();
        }
    }

    public static Map<String, TypeMirror> getDataFields(RestMethodData.ParameterInfo param, DocletEnvironment environment) {

        Map<String, TypeMirror> dataFields = new LinkedHashMap<>();

        if (equalsClass(param.entityClass, FormParameters.class, environment)) {
            // manually grouped form parameters
            param.nestedParameters
                    .forEach(np -> dataFields.put(np.name, np.displayClass != null ? np.displayClass : np.entityClass));

        } else {

            dataFields.putAll(getDataFields(param.displayClass != null ? param.displayClass : param.entityClass, environment));
        }

        return dataFields;
    }

    private static boolean equalsClass(TypeMirror typeMirror, Class<?> otherClass, DocletEnvironment environment) {
        if (typeMirror.getKind().isPrimitive()) {
            return false;
        }

        Element element = environment.getTypeUtils().asElement(typeMirror);

        if (element == null) return false;

        PackageElement packageElement = environment.getElementUtils().getPackageOf(element);

        return element.getSimpleName().toString().equals(otherClass.getSimpleName())
                && packageElement.getQualifiedName().toString().equals(otherClass.getPackageName());
    }

    public static Map<String, TypeMirror> getDataFields(TypeMirror type, DocletEnvironment environment) {
        if (type.getKind().isPrimitive()) {
            return Collections.emptyMap();
        }

        Map<String, TypeMirror> dataFields = new LinkedHashMap<>();

        List<VariableElement> fields = getVisibleFields(type, environment);

        fields.forEach(field -> {
            TypeMirror fieldType = field.asType();
            dataFields.put(getPublicName(field), fieldType);
        });

        if (fields.isEmpty()) {
            getGetters(type, dataFields, environment);
        }

        Optional<? extends TypeMirror> superTypeMirror = environment.getTypeUtils().directSupertypes(type).stream().findFirst();

        if (superTypeMirror.isPresent() && !isJavaType(superTypeMirror.get(), environment)) {
            dataFields.putAll(getDataFields(superTypeMirror.get(), environment));
        }

        return dataFields;
    }

    private static void getGetters(TypeMirror type, Map<String, TypeMirror> dataFields, DocletEnvironment environment) {
        if (type.getKind().isPrimitive()) {
            return;
        }

        List<ExecutableElement> getters = getVisibleGetters(type, environment);

        if (!isJavaType(type, environment)) {

            getters.forEach(getter -> {
                TypeMirror fieldType = getter.getReturnType();
                dataFields.put(getNameFromGetter(getter, environment), fieldType);
            });
        }

        if (getters.isEmpty()) {
            ExecutableElement chosenCtor = null;

            var element = (TypeElement) environment.getTypeUtils().asElement(type);
            List<ExecutableElement> ctors = environment.getElementUtils().getAllMembers(element).stream()
                    .filter(e -> e instanceof ExecutableElement)
                    .map(e -> (ExecutableElement) e)
                    .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                    .collect(Collectors.toList());


            for (ExecutableElement ctor : ctors) {
                if (chosenCtor == null) {
                    chosenCtor = ctor;
                } else if (getVisibleCtorParameters(ctor).size() > getVisibleCtorParameters(chosenCtor).size()) {
                    chosenCtor = ctor;
                }
            }

            if (chosenCtor != null) {
                for (VariableElement param : getVisibleCtorParameters(chosenCtor)) {
                    dataFields.put(getPublicName(param), param.asType());
                }
            }
        }
    }

    public static boolean isArrayType(TypeMirror type) {
        return type instanceof ArrayType;
    }

    public static boolean isCollectionType(TypeMirror type) {
        return type != null
                && (type instanceof ArrayType
                || type.toString().startsWith("java.util.List")
                || type.toString().startsWith("java.util.Set"));
    }

    public static boolean isMapType(TypeMirror type) {
        return type != null && type.toString().startsWith("java.util.Map");
    }

    public static String getTypeString(TypeMirror type, DocletEnvironment environment) {
        if (type.getKind().isPrimitive()) {
            return getPrimitiveTypeString(type);
        }

        Element element = environment.getTypeUtils().asElement(type);

        if (element == null) {
            return "String";
        }

        if (isEnum(type, environment)) {
            StringJoiner sj = new StringJoiner("|");
            getEnumValuesAsList(type, environment).forEach(ev -> sj.add(ev.getSimpleName().toString()));
            return sj.toString();
        }

        String name = element.getSimpleName().toString();

        if (isArrayType(type)) {
            name += "[]";
        }

        //printing out "byte[]" as type may confuse people, so change it to String
        if (name.equals("byte[]")) {
            return "String";
        }

        return name;
    }

    public static String getSimpleTypeName(TypeMirror type, DocletEnvironment environment) {
        if (type.getKind().isPrimitive()) {
            return getPrimitiveTypeString(type);
        }

        Element element = environment.getTypeUtils().asElement(type);

        if (element == null) {
            return "String";
        }

        String simpleName = getPublicName(element);

        if (isCollectionType(type)) {
            List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
            return simpleName + "[" + getSimpleTypeName(typeArguments.get(0), environment) + "]";
        } else if (isMapType(type)) {
            List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
            return simpleName + "["
                    + getSimpleTypeName(typeArguments.get(0), environment)
                    + ", "
                    + getSimpleTypeName(typeArguments.get(1), environment)
                    + "]";
        } else {
            return simpleName;
        }
    }

    public static String getPublicName(Element element) {

        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();

        String publicName = null;

        if (hasAnnotation(annotationMirrors, XML_ROOT_ELEMENT)) {
            publicName = getAnnotationValue(annotationMirrors, XML_ROOT_ELEMENT, "name");
        } else if (hasAnnotation(annotationMirrors, JACKSON_XML_ROOT_ELEMENT)) {
            publicName = getAnnotationValue(annotationMirrors, JACKSON_XML_ROOT_ELEMENT, "localName");
        }

        if (publicName == null) {
            publicName = element.getSimpleName().toString();
        }

        return publicName;
    }

    private static String getPrimitiveTypeString(TypeMirror type) {
        switch (type.getKind()) {
            case BOOLEAN:
                return "boolean";
            case BYTE:
                return "byte";
            case SHORT:
                return "short";
            case INT:
                return "int";
            case LONG:
                return "long";
            case CHAR:
                return "char";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            case VOID:
                return "void";
            default:
                return "unknown";
        }
    }

    public static boolean typeCantBeDocumented(TypeMirror type, Options options) {
        if (type != null && equalsClass(type, FormParameters.class, options.environment)) {
            return false;
        }

        Element element = options.environment.getTypeUtils().asElement(type);

        return type == null
                || element == null
                || type.getKind().isPrimitive()
                || getPackageName(element, options.environment).startsWith("java.")
                || containedFieldNamesAreNotAvailableOrPackageExcluded(type, options);
    }

    public static boolean isJavaType(TypeMirror type, DocletEnvironment environment) {
        var element = environment.getTypeUtils().asElement(type);
        var packageName = getPackageName(element, environment);
        return packageName.startsWith("java.");
    }

    public static List<VariableElement> getVisibleFields(TypeMirror type, DocletEnvironment environment) {
        if (type.getKind().isPrimitive()) {
            return Collections.emptyList();
        }

        var element = (TypeElement) environment.getTypeUtils().asElement(type);
        List<VariableElement> fields = environment.getElementUtils().getAllMembers(element).stream()
                .filter(e -> e instanceof VariableElement)
                .map(e -> (VariableElement) e)
                .filter(e -> e.getModifiers().contains(Modifier.PUBLIC))
                .collect(Collectors.toList());

        return fields.stream().filter(JavaDocUtils::fieldIsVisible).collect(Collectors.toList());
    }

    private static boolean fieldIsVisible(VariableElement field) {
        return !hasDirectAnnotation(field, ApiIgnore.class)
                && !hasDirectAnnotation(field, XML_TRANSIENT)
                && !hasDirectAnnotation(field, JSON_IGNORE)
                && !hasDirectAnnotation(field, JSON_IGNORE_LEGACY)
                && !field.getModifiers().contains(Modifier.STATIC)
                && !field.getModifiers().contains(Modifier.TRANSIENT)
                && field.getModifiers().contains(Modifier.PUBLIC);
    }

    public static List<ExecutableElement> getVisibleGetters(TypeMirror type, DocletEnvironment environment) {
        if (isJavaType(type, environment) || type.getKind().isPrimitive()) {
            return Collections.emptyList();
        }

        var element = (TypeElement) environment.getTypeUtils().asElement(type);
        List<ExecutableElement> methods = environment.getElementUtils().getAllMembers(element).stream()
                .filter(e -> e instanceof ExecutableElement)
                .map(e -> (ExecutableElement) e)
                .filter(e -> e.getReturnType() != null)
                .filter(e -> environment.getTypeUtils().isSameType(element.asType(), e.getEnclosingElement().asType()))
                .collect(Collectors.toList());

        return methods.stream().filter(e -> methodIsVisibleGetter(e, environment)).collect(Collectors.toList());
    }

    private static boolean methodIsVisibleGetter(ExecutableElement method, DocletEnvironment environment) {
        return !hasAnnotation(method, ApiIgnore.class, environment)
                && !hasAnnotation(method, XML_TRANSIENT, environment)
                && !hasAnnotation(method, JSON_IGNORE, environment)
                && !hasAnnotation(method, JSON_IGNORE_LEGACY, environment)
                && method.getSimpleName().toString().startsWith("get")
                && !method.getSimpleName().toString().equals("get") // ignore get() methods
                && !method.getModifiers().contains(Modifier.STATIC)
                && !method.getModifiers().contains(Modifier.TRANSIENT)
                && method.getModifiers().contains(Modifier.PUBLIC);
    }

    public static List<VariableElement> getVisibleCtorParameters(ExecutableElement chosenCtor) {
        return chosenCtor.getParameters().stream().filter(JavaDocUtils::parameterIsVisible).collect(Collectors.toList());
    }

    private static boolean parameterIsVisible(VariableElement param) {
        return !hasDirectAnnotation(param, ApiIgnore.class);
    }

    public static String getNameFromGetter(ExecutableElement getter, DocletEnvironment environment) {
        if (hasAnnotation(getter, JSON_PROPERTY, environment)) {
            return getAnnotationValue(getter, JSON_PROPERTY, "value", environment);
        } else if (hasAnnotation(getter, JSON_PROPERTY_LEGACY, environment)) {
            return getAnnotationValue(getter, JSON_PROPERTY_LEGACY, "value", environment);
        } else {
            String simpleName = getter.getSimpleName().toString();
            return simpleName.substring(3, 4).toLowerCase() + simpleName.substring(4);
        }
    }

    public static List<TypeElement> getTypeElements(DocletEnvironment environment) {
        return environment.getIncludedElements().stream()
                .filter(e -> e instanceof TypeElement)
                .map(e -> (TypeElement) e)
                .collect(Collectors.toList());
    }

    public static List<ExecutableElement> getExecutableElements(Element element) {
        return element.getEnclosedElements().stream()
                .filter(e -> e instanceof ExecutableElement)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());
    }

    public static List<ExecutableElement> getConstructors(TypeMirror typeMirror, DocletEnvironment environment) {
        return getConstructors(environment.getTypeUtils().asElement(typeMirror));
    }

    public static List<ExecutableElement> getConstructors(Element element) {
        return getExecutableElements(element).stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .collect(Collectors.toList());
    }

    public static String getCommentText(Element element, DocletEnvironment environment, boolean stripInlineTags) {
        if (!stripInlineTags) {
            return Optional.ofNullable(environment.getElementUtils().getDocComment(element)).orElse("");

        }

        DocCommentTree docCommentTree = environment.getDocTrees().getDocCommentTree(element);

        if (docCommentTree == null) {
            return "";
        }

        return getCommentTextWithoutInlineTags(docCommentTree.getFullBody());
    }

    public static String getCommentTextWithoutInlineTags(List<? extends DocTree> description) {
        return description.stream()
                .filter(tag -> tag instanceof TextTree || tag instanceof StartElementTree || tag instanceof EndElementTree)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public static boolean isEnum(TypeMirror typeMirror, DocletEnvironment environment) {
        Element element = environment.getTypeUtils().asElement(typeMirror);
        return element != null && element.getKind() == ElementKind.ENUM;
    }

    public static String getPackageName(TypeMirror typeMirror, DocletEnvironment environment) {
        Element element = environment.getTypeUtils().asElement(typeMirror);
        return getPackageName(element, environment);
    }

    public static String getPackageName(Element element, DocletEnvironment environment) {
        return environment.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }

    public static String getQualifiedName(TypeMirror typeMirror, DocletEnvironment environment) {
        if (typeMirror.getKind() == TypeKind.ARRAY) {
            return getQualifiedName(((ArrayType) typeMirror).getComponentType(), environment) + "[]";
        }

        Element element = environment.getTypeUtils().asElement(typeMirror);
        return getPackageName(element, environment) + "." + element.getSimpleName().toString();
    }

    public static Element asElement(TypeMirror typeMirror, DocletEnvironment environment) {
        return environment.getTypeUtils().asElement(typeMirror);
    }

    public static List<VariableElement> getEnumValuesAsList(TypeMirror enumType, DocletEnvironment environment) {
        Element element = environment.getTypeUtils().asElement(enumType);
        return getEnumValuesAsList((TypeElement) element, environment);
    }

    public static List<VariableElement> getEnumValuesAsList(TypeElement element, DocletEnvironment environment) {
        return environment.getElementUtils().getAllMembers(element)
                .stream().filter(m -> m.getKind() == ElementKind.ENUM_CONSTANT)
                .map(m -> (VariableElement) m)
                .collect(Collectors.toList());
    }

    public static List<? extends DocTree> getBlockTags(Element parameter, DocletEnvironment environment) {
        DocCommentTree docCommentTree = environment.getDocTrees().getDocCommentTree(parameter);
        return docCommentTree == null ? Collections.emptyList() : docCommentTree.getBlockTags();
    }

    public static List<? extends DocTree> getInlineTags(Element parameter, DocletEnvironment environment) {
        DocCommentTree docCommentTree = environment.getDocTrees().getDocCommentTree(parameter);

        return docCommentTree == null
               ? Collections.emptyList()
               : docCommentTree.getFullBody().stream().filter(d -> d.getKind() != DocTree.Kind.TEXT).collect(Collectors.toList());
    }

    public static Element getReferencedElement(Element e, DocTree dtree, DocletEnvironment environment) {
        TreePath elementPath = environment.getDocTrees().getPath(e);
        DocCommentTree docCommentTree = environment.getDocTrees().getDocCommentTree(e);

        if (elementPath == null || docCommentTree == null) {
            return null;
        }

        DocTreePath path = DocTreePath.getPath(elementPath, docCommentTree, dtree);
        return environment.getDocTrees().getElement(path);
    }

}
