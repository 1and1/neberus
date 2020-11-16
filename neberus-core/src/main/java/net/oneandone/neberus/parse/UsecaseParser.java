package net.oneandone.neberus.parse;

import net.oneandone.neberus.Options;
import net.oneandone.neberus.annotation.ApiUsecase;
import net.oneandone.neberus.annotation.ApiUsecases;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.oneandone.neberus.util.JavaDocUtils.extractValue;
import static net.oneandone.neberus.util.JavaDocUtils.getCommentTextFromInterfaceOrClass;
import static net.oneandone.neberus.util.JavaDocUtils.getInterfaceClass;

/**
 * Parses usecase related things.
 */
public class UsecaseParser {

    private final Options options;


    public UsecaseParser(Options options) {
        this.options = options;
    }

    public RestUsecaseData parse(TypeElement typeElement, List<RestClassData> restClasses) {
        RestUsecaseData restUsecaseData = new RestUsecaseData();

        restUsecaseData.description = getCommentTextFromInterfaceOrClass(typeElement, options.environment, false);

        getUsecaseAnnotations(typeElement)
                .forEach(usecaseAnnotation -> addUsecase(restUsecaseData, usecaseAnnotation, restClasses, typeElement));

        return restUsecaseData;
    }

    private void addUsecase(RestUsecaseData restUsecaseData, AnnotationMirror usecaseAnnotation,
                            List<RestClassData> restClasses, TypeElement typeElement) {

        String name = extractValue(usecaseAnnotation, "name");
        String description = extractValue(usecaseAnnotation, "description");
        List<AnnotationValue> usecaseMethods = extractValue(usecaseAnnotation, "methods");

        int idInt = (name == null ? 1 : name.hashCode())
                + (description == null ? 1 : description.hashCode())
                + typeElement.getQualifiedName().toString().hashCode();
        String id = String.valueOf(idInt);

        RestUsecaseData.UsecaseData usecaseData = new RestUsecaseData.UsecaseData(id);
        restUsecaseData.usecases.add(usecaseData);

        usecaseData.name = name;
        usecaseData.description = description != null ? description : "";

        if (usecaseMethods != null) {
            usecaseMethods.forEach(method -> addMethod(usecaseData, method, restClasses));
        }
    }

    private void addMethod(RestUsecaseData.UsecaseData usecaseData, AnnotationValue method, List<RestClassData> restClasses) {
        AnnotationMirror methodDesc = (AnnotationMirror) method.getValue();
        String methodPath = extractValue(methodDesc, "path");
        String methodHttpMethod = extractValue(methodDesc, "httpMethod");
        String methodDescription = extractValue(methodDesc, "description");

        RestUsecaseData.UsecaseMethodData usecaseMethodData = new RestUsecaseData.UsecaseMethodData();
        usecaseData.methods.add(usecaseMethodData);

        usecaseMethodData.path = methodPath;
        usecaseMethodData.httpMethod = methodHttpMethod;
        usecaseMethodData.description = methodDescription;

        addLinkedMethod(restClasses, usecaseMethodData);
        addParameters(methodDesc, usecaseMethodData);
        addRequestBody(methodDesc, usecaseMethodData);
        addResponseBody(methodDesc, usecaseMethodData);
    }

    private void addResponseBody(AnnotationMirror methodDesc, RestUsecaseData.UsecaseMethodData usecaseMethodData) {
        List<AnnotationValue> responseBody = extractValue(methodDesc, "responseBody");
        if (responseBody != null) {
            AnnotationMirror responseValueDesc = (AnnotationMirror) responseBody.get(0).getValue();
            String responseValueContentType = extractValue(responseValueDesc, "contentType");
            String responseValueValue = extractValue(responseValueDesc, "value");
            String responseValueValueHint = extractValue(responseValueDesc, "valueHint");
            usecaseMethodData.responseBody.put(responseValueContentType,
                    new RestUsecaseData.UsecaseValueInfo(responseValueValue, responseValueValueHint));
        }
    }

    private void addRequestBody(AnnotationMirror methodDesc, RestUsecaseData.UsecaseMethodData usecaseMethodData) {
        List<AnnotationValue> requestBody = extractValue(methodDesc, "requestBody");
        if (requestBody != null) {
            AnnotationMirror responseValueDesc = (AnnotationMirror) requestBody.get(0).getValue();
            String responseValueContentType = extractValue(responseValueDesc, "contentType");
            String responseValueValue = extractValue(responseValueDesc, "value");
            String responseValueValueHint = extractValue(responseValueDesc, "valueHint");
            usecaseMethodData.requestBody.put(responseValueContentType,
                    new RestUsecaseData.UsecaseValueInfo(responseValueValue, responseValueValueHint));
        }
    }

    private void addParameters(AnnotationMirror methodDesc, RestUsecaseData.UsecaseMethodData usecaseMethodData) {
        List<AnnotationValue> parameters = extractValue(methodDesc, "parameters");
        if (parameters != null) {
            parameters.forEach(parameter -> {
                AnnotationMirror parameterDesc = (AnnotationMirror) parameter.getValue();
                String paramName = extractValue(parameterDesc, "name");
                String paramValue = extractValue(parameterDesc, "value");
                String paramValueHint = extractValue(parameterDesc, "valueHint");

                usecaseMethodData.parameters.put(paramName, new RestUsecaseData.UsecaseValueInfo(paramValue, paramValueHint));
            });
        }
    }

    //TODO map/replace arbitrary parameter names (used in the povided path) to actually used names in the linked method
    private void addLinkedMethod(List<RestClassData> restClasses, RestUsecaseData.UsecaseMethodData usecaseMethodData) {
        // find possibly linked method
        usecaseMethodData.linkedMethod = restClasses.stream().flatMap(rc -> rc.methods.stream())
                .filter(m -> m.methodData.path.equals(usecaseMethodData.path)
                        && m.methodData.httpMethod.equals(usecaseMethodData.httpMethod))
                .findFirst().orElse(null);
    }

    private List<? extends AnnotationMirror> getUsecaseAnnotations(TypeElement classDoc) {
        Optional<TypeElement> interfaceClass = getInterfaceClass(classDoc, options.environment);

        List<AnnotationMirror> annotations = new ArrayList<>(classDoc.getAnnotationMirrors());

        if (interfaceClass.isPresent()) {
            annotations.addAll(interfaceClass.get().getAnnotationMirrors());
        }

        return annotations.stream()
                .flatMap(a -> {
                    if (((TypeElement) a.getAnnotationType().asElement()).getQualifiedName().toString().equals(ApiUsecases.class.getName())) {
                        List<AnnotationValue> values = extractValue(a, "value");

                        return values.stream().map(method -> (AnnotationMirror) method.getValue());
                    }
                    return Stream.of(a);
                })
                .filter(a -> ((TypeElement) a.getAnnotationType().asElement()).getQualifiedName().toString().equals(ApiUsecase.class.getName()))
                .collect(Collectors.toList());
    }

}
