package net.oneandone.neberus.parse;

import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiHeader;
import net.oneandone.neberus.annotation.ApiHeaders;
import net.oneandone.neberus.annotation.ApiIgnore;
import net.oneandone.neberus.annotation.ApiLabel;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.oneandone.neberus.util.JavaDocUtils.extractValue;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationDesc;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.getCommentTextFromInterfaceOrClass;
import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;

/**
 * Parses class related things.
 */
public abstract class ClassParser {

    MethodParser methodParser;

    public ClassParser(MethodParser methodParser) {
        this.methodParser = methodParser;
    }

    public RestClassData parse(TypeElement classDoc) {

        RestClassData restClassData = new RestClassData();

        // class related stuff
        addLabel(classDoc, restClassData);
        addHeaders(classDoc, restClassData);
        addDescription(classDoc, restClassData);

        restClassData.className = classDoc.getSimpleName().toString();
        restClassData.classDoc = classDoc;

        // contained methods
        List<ExecutableElement> methods = classDoc.getEnclosedElements().stream()
                .filter(e -> e instanceof ExecutableElement)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());

        for (ExecutableElement method : methods) {
            RestMethodData parsedMethodData = parseMethod(method);
            if (parsedMethodData != null) {
                parsedMethodData.containingClass = restClassData;
                restClassData.methods.add(parsedMethodData);
            }
        }

        return restClassData;
    }

    protected abstract String getHttpMethod(ExecutableElement method);

    private RestMethodData parseMethod(ExecutableElement method) {
        String httpMethod = getHttpMethod(method);

        if (httpMethod == null || hasAnnotation(method, ApiIgnore.class, methodParser.options.environment)) {
            return null;
        }

        return methodParser.parseMethod(method, httpMethod);
    }

    /**
     * Use the value defined in {@link ApiLabel} or use the name of the class.
     *
     * @param classDoc      classDoc
     * @param restClassData restClassData
     */
    protected void addLabel(TypeElement classDoc, RestClassData restClassData) {
        String label = getAnnotationValue(classDoc, ApiLabel.class, "value", methodParser.options.environment);
        if (label != null) {
            restClassData.label = label;
        } else {
            restClassData.label = classDoc.getSimpleName().toString();
        }
    }

    /**
     * Use the value defined in {@link ApiDescription} or use the javadoc comment of the class.
     *
     * @param classDoc      classDoc
     * @param restClassData restClassData
     */
    protected void addDescription(TypeElement classDoc, RestClassData restClassData) {
        String description = getAnnotationValue(classDoc, ApiDescription.class, "value", methodParser.options.environment);

        if (description != null) {
            restClassData.description = description;
        } else {
            restClassData.description = getCommentTextFromInterfaceOrClass(classDoc, methodParser.options.environment, false);
        }
    }

    protected void addHeaders(TypeElement classDoc, RestClassData restClassData) {
        List<AnnotationValue> headers = getAnnotationValue(classDoc, ApiHeaders.class, "value", methodParser.options.environment);
        if (headers != null) {
            //more than one annotation is defined, so we got the container
            headers.forEach(header -> addHeader((AnnotationMirror) header.getValue(), restClassData));
        } else {
            //check if a single annotation is defined
            Optional<? extends AnnotationMirror> singleResponse = getAnnotationDesc(classDoc, ApiHeader.class, methodParser.options.environment);
            singleResponse.ifPresent(annotationDesc -> addHeader(annotationDesc, restClassData));
        }
    }

    protected void addHeader(AnnotationMirror headerDesc, RestClassData restClassData) {
        String name = extractValue(headerDesc, "name");
        String description = extractValue(headerDesc, "description");

        RestMethodData.HeaderInfo headerInfo = new RestMethodData.HeaderInfo();

        headerInfo.name = name;
        headerInfo.description = description;

        restClassData.headerDefinitions.put(name, headerInfo);
    }

}
