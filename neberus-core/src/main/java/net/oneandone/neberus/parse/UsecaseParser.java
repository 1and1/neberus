package net.oneandone.neberus.parse;

import net.oneandone.neberus.Options;
import net.oneandone.neberus.annotation.ApiUsecase;
import net.oneandone.neberus.annotation.ApiUsecases;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
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
                .forEach(usecaseAnnotation -> addUsecase(restUsecaseData, usecaseAnnotation, restClasses));

        return restUsecaseData;
    }

    private void addUsecase(RestUsecaseData restUsecaseData, AnnotationMirror usecaseAnnotation, List<RestClassData> restClasses) {
        String name = extractValue(usecaseAnnotation, "name");
        String description = extractValue(usecaseAnnotation, "description");
        List<AnnotationValue> usecaseMethods = extractValue(usecaseAnnotation, "methods");

        RestUsecaseData.UsecaseData usecaseData = new RestUsecaseData.UsecaseData();
        restUsecaseData.usecases.add(usecaseData);

        usecaseData.name = name;
        usecaseData.description = description != null ? description : "";

        if (usecaseMethods != null) {
            usecaseMethods.forEach(method -> addMethod(usecaseData, method, restClasses));
        }
    }

    private void addMethod(RestUsecaseData.UsecaseData usecaseData, AnnotationValue method, List<RestClassData> restClasses) {
        AnnotationMirror methodDesc = (AnnotationMirror) method.getValue();
        String methodName = extractValue(methodDesc, "name");
        String methodDescription = extractValue(methodDesc, "description");

        RestUsecaseData.UsecaseMethodData usecaseMethodData = new RestUsecaseData.UsecaseMethodData();
        usecaseData.methods.add(usecaseMethodData);

        usecaseMethodData.name = methodName;
        usecaseMethodData.description = methodDescription;

        addLinkedMethod(methodDesc, restClasses, usecaseMethodData, methodName);
        addParameters(methodDesc, usecaseMethodData);
        addResponseValue(methodDesc, usecaseMethodData);
    }

    private void addResponseValue(AnnotationMirror methodDesc, RestUsecaseData.UsecaseMethodData usecaseMethodData) {
        List<AnnotationValue> responseValue = extractValue(methodDesc, "responseValue");
        if (responseValue != null) {
            AnnotationMirror responseValueDesc = (AnnotationMirror) responseValue.get(0).getValue();
            String responseValueName = extractValue(responseValueDesc, "name");
            String responseValueValue = extractValue(responseValueDesc, "value");
            String responseValueValueHint = extractValue(responseValueDesc, "valueHint");
            usecaseMethodData.responseValue.put(responseValueName,
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

    private void addLinkedMethod(AnnotationMirror methodDesc, List<RestClassData> restClasses,
                                 RestUsecaseData.UsecaseMethodData usecaseMethodData, String methodName) {
        // find possibly linked method
        DeclaredType linkedClass = extractValue(methodDesc, "restClass");


        if (linkedClass != null) {
            Optional<RestClassData> linkedRestClass =
                    restClasses.stream()
                            .filter(rc -> {
                                Optional<TypeElement> interfaceClass = getInterfaceClass(rc.classDoc, options.environment);

                                return options.environment.getTypeUtils().isSameType(linkedClass.asElement().asType(), rc.classDoc.asType())
                                        || interfaceClass.isPresent()
                                        && options.environment.getTypeUtils().isSameType(linkedClass.asElement().asType(),
                                        interfaceClass.get().asType());
                            })
                            .findFirst();

            if (linkedRestClass.isPresent()) {
                usecaseMethodData.linkedMethod = linkedRestClass.get().methods.stream()
                        .filter(m -> m.methodData.label.equals(methodName)).findFirst().orElse(null);
            }
        }
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
