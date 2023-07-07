package net.oneandone.neberus.parse;

import net.oneandone.neberus.annotation.ApiCommonResponse;
import net.oneandone.neberus.annotation.ApiCommonResponses;
import net.oneandone.neberus.annotation.ApiCookieDefinition;
import net.oneandone.neberus.annotation.ApiCookieDefinitions;
import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiHeaderDefinition;
import net.oneandone.neberus.annotation.ApiHeaderDefinitions;
import net.oneandone.neberus.annotation.ApiIgnore;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.model.CookieSameSite;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.oneandone.neberus.parse.MethodParser.VALUE;
import static net.oneandone.neberus.util.JavaDocUtils.extractValue;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationDesc;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.getCommentTextFromInterfaceOrClass;
import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;

/**
 * Parses class related things.
 */
@SuppressWarnings("PMD.TooManyStaticImports")
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
        addCookies(classDoc, restClassData);
        addResponsesFromAnnotations(classDoc, restClassData);
        addDescription(classDoc, restClassData);

        restClassData.className = classDoc.getSimpleName().toString();
        restClassData.classDoc = classDoc;

        // contained methods
        List<ExecutableElement> methods = classDoc.getEnclosedElements().stream()
                .filter(e -> e instanceof ExecutableElement)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());

        for (ExecutableElement method : methods) {
            List<RestMethodData> parsedMethodDataList = parseMethods(method);
            if (parsedMethodDataList != null) {
                parsedMethodDataList.forEach(parsedMethodData -> {
                    parsedMethodData.containingClass = restClassData;
                    restClassData.methods.add(parsedMethodData);
                });
            }
        }

        return restClassData;
    }

    protected abstract List<String> getHttpMethods(ExecutableElement method);

    private List<RestMethodData> parseMethods(ExecutableElement method) {
        List<String> httpMethods = getHttpMethods(method);

        if (httpMethods == null || hasAnnotation(method, ApiIgnore.class, methodParser.options.environment)) {
            return Collections.emptyList();
        }

        return httpMethods.stream().map(httpMethod -> methodParser.parseMethod(method, httpMethod)).collect(Collectors.toList());
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
     * Use the value defined in {@link ApiDescription} and use the javadoc comment of the class.
     *
     * @param classDoc      classDoc
     * @param restClassData restClassData
     */
    protected void addDescription(TypeElement classDoc, RestClassData restClassData) {
        restClassData.shortDescription = getAnnotationValue(classDoc, ApiDescription.class, "value", methodParser.options.environment);

        restClassData.description = getCommentTextFromInterfaceOrClass(classDoc, methodParser.options.environment, false);
    }

    protected void addHeaders(TypeElement classDoc, RestClassData restClassData) {
        List<AnnotationValue> headers = getAnnotationValue(classDoc, ApiHeaderDefinitions.class, "value", methodParser.options.environment);
        if (headers != null) {
            //more than one annotation is defined, so we got the container
            headers.forEach(header -> addHeader((AnnotationMirror) header.getValue(), restClassData));
        } else {
            //check if a single annotation is defined
            List<? extends AnnotationMirror> singleResponse = getAnnotationDesc(classDoc, ApiHeaderDefinition.class, methodParser.options.environment);
            singleResponse.forEach(annotationDesc -> addHeader(annotationDesc, restClassData));
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

    protected void addCookies(TypeElement classDoc, RestClassData restClassData) {
        List<AnnotationValue> cookies = getAnnotationValue(classDoc, ApiCookieDefinitions.class, "value", methodParser.options.environment);
        if (cookies != null) {
            //more than one annotation is defined, so we got the container
            cookies.forEach(cookie -> addCookie((AnnotationMirror) cookie.getValue(), restClassData));
        } else {
            //check if a single annotation is defined
            List<? extends AnnotationMirror> singleResponse = getAnnotationDesc(classDoc, ApiCookieDefinition.class, methodParser.options.environment);
            singleResponse.forEach(annotationDesc -> addCookie(annotationDesc, restClassData));
        }
    }

    protected void addCookie(AnnotationMirror cookieDesc, RestClassData restClassData) {
        RestMethodData.CookieInfo cookieInfo = new RestMethodData.CookieInfo();

        cookieInfo.name = extractValue(cookieDesc, "name");
        cookieInfo.description = extractValue(cookieDesc, "description");
        VariableElement sameSite = extractValue(cookieDesc, "sameSite");
        cookieInfo.sameSite = sameSite == null
                              ? CookieSameSite.UNSET
                              : CookieSameSite.valueOf(sameSite.getSimpleName().toString());
        cookieInfo.domain = extractValue(cookieDesc, "domain");
        cookieInfo.path = extractValue(cookieDesc, "path");
        cookieInfo.maxAge = extractValue(cookieDesc, "maxAge");
        cookieInfo.secure = extractValue(cookieDesc, "secure");
        cookieInfo.httpOnly = extractValue(cookieDesc, "httpOnly");

        restClassData.cookieDefinitions.put(cookieInfo.name, cookieInfo);
    }

    protected void addResponsesFromAnnotations(TypeElement classDoc, RestClassData restClassData) {
        // the first value from @Produces may be used as Content-Type, if no specific one is defined.
        List<AnnotationValue> produces = Collections.emptyList();
        RestMethodData tmpRestMethodData = new RestMethodData("tmp");

        //check for the (maybe implicit) container annotation...
        List<? extends AnnotationMirror> responses = getAnnotationDesc(classDoc, ApiCommonResponses.class, methodParser.options.environment);
        if (responses != null) {
            //...and iterate over it's content
            responses.stream().flatMap(container -> {
                List<AnnotationValue> vs = extractValue(container, VALUE);
                if (vs == null) {
                    return Stream.empty();
                } else {
                    return vs.stream();
                }
            }).forEach(repsonse -> methodParser.addResponse((AnnotationMirror) repsonse.getValue(), tmpRestMethodData, produces));
        }

        //also look for single annotation(s)
        List<? extends AnnotationMirror> singleResponse = getAnnotationDesc(classDoc, ApiCommonResponse.class, methodParser.options.environment);
        singleResponse.forEach(annotationDesc -> methodParser.addResponse(annotationDesc, tmpRestMethodData, produces));

        restClassData.commonResponseData.addAll(tmpRestMethodData.responseData);
    }

}
