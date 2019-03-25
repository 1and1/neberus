package net.oneandone.neberus.parse;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import net.oneandone.neberus.annotation.ApiUsecase;
import net.oneandone.neberus.annotation.ApiUsecases;
import net.oneandone.neberus.util.JavaDocUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses usecase related things.
 */
public class UsecaseParser {

    public RestUsecaseData parse(ClassDoc classDoc, List<RestClassData> restClasses) {
        RestUsecaseData restUsecaseData = new RestUsecaseData();

        restUsecaseData.description = classDoc.commentText();

        getUsecaseAnnotations(classDoc)
                .forEach(usecaseAnnotation -> addUsecase(restUsecaseData, usecaseAnnotation, restClasses));

        return restUsecaseData;
    }

    private void addUsecase(RestUsecaseData restUsecaseData, AnnotationDesc usecaseAnnotation, List<RestClassData> restClasses) {
        String name = JavaDocUtils.extractValue(usecaseAnnotation, "name");
        String description = JavaDocUtils.extractValue(usecaseAnnotation, "description");
        AnnotationValue[] usecaseMethods = JavaDocUtils.extractValue(usecaseAnnotation, "methods");

        RestUsecaseData.UsecaseData usecaseData = new RestUsecaseData.UsecaseData();
        restUsecaseData.usecases.add(usecaseData);

        usecaseData.name = name;
        usecaseData.description = description != null ? description : "";

        if (usecaseMethods != null) {
            Stream.of(usecaseMethods).forEach(method -> addMethod(usecaseData, method, restClasses));
        }
    }

    private void addMethod(RestUsecaseData.UsecaseData usecaseData, AnnotationValue method, List<RestClassData> restClasses) {
        AnnotationDesc methodDesc = (AnnotationDesc) method.value();
        String methodName = JavaDocUtils.extractValue(methodDesc, "name");
        String methodDescription = JavaDocUtils.extractValue(methodDesc, "description");

        RestUsecaseData.UsecaseMethodData usecaseMethodData = new RestUsecaseData.UsecaseMethodData();
        usecaseData.methods.add(usecaseMethodData);

        usecaseMethodData.name = methodName;
        usecaseMethodData.description = methodDescription;

        addLinkedMethod(methodDesc, restClasses, usecaseMethodData, methodName);
        addParameters(methodDesc, usecaseMethodData);
        addResponseValue(methodDesc, usecaseMethodData);
    }

    private void addResponseValue(AnnotationDesc methodDesc, RestUsecaseData.UsecaseMethodData usecaseMethodData) {
        AnnotationValue[] responseValue = JavaDocUtils.extractValue(methodDesc, "responseValue");
        if (responseValue != null) {
            AnnotationDesc responseValueDesc = (AnnotationDesc) responseValue[0].value();
            String responseValueName = JavaDocUtils.extractValue(responseValueDesc, "name");
            String responseValueValue = JavaDocUtils.extractValue(responseValueDesc, "value");
            String responseValueValueHint = JavaDocUtils.extractValue(responseValueDesc, "valueHint");
            usecaseMethodData.responseValue.put(responseValueName,
                    new RestUsecaseData.UsecaseValueInfo(responseValueValue, responseValueValueHint));
        }
    }

    private void addParameters(AnnotationDesc methodDesc, RestUsecaseData.UsecaseMethodData usecaseMethodData) {
        AnnotationValue[] parameters = JavaDocUtils.extractValue(methodDesc, "parameters");
        if (parameters != null) {
            Stream.of(parameters).forEach(parameter -> {
                AnnotationDesc parameterDesc = (AnnotationDesc) parameter.value();
                String paramName = JavaDocUtils.extractValue(parameterDesc, "name");
                String paramValue = JavaDocUtils.extractValue(parameterDesc, "value");
                String paramValueHint = JavaDocUtils.extractValue(parameterDesc, "valueHint");

                usecaseMethodData.parameters.put(paramName, new RestUsecaseData.UsecaseValueInfo(paramValue, paramValueHint));
            });
        }
    }

    private void addLinkedMethod(AnnotationDesc methodDesc, List<RestClassData> restClasses,
                                 RestUsecaseData.UsecaseMethodData usecaseMethodData, String methodName) {
        // find possibly linked method
        ClassDoc linkedClass = JavaDocUtils.extractValue(methodDesc, "restClass");
        if (linkedClass != null) {
            Optional<RestClassData> linkedRestClass =
                    restClasses.stream()
                            .filter(rc -> rc.classDoc.equals(linkedClass)
                                    || (JavaDocUtils.getInterfaceClass(rc.classDoc).isPresent()
                                    && JavaDocUtils.getInterfaceClass(rc.classDoc).get().equals(linkedClass)))
                            .findFirst();

            if (linkedRestClass.isPresent()) {
                usecaseMethodData.linkedMethod = linkedRestClass.get().methods.stream()
                        .filter(m -> m.methodData.label.equals(methodName)).findFirst().orElse(null);
            }
        }
    }

    private List<AnnotationDesc> getUsecaseAnnotations(ClassDoc classDoc) {
        Optional<ClassDoc> interfaceClass = JavaDocUtils.getInterfaceClass(classDoc);

        List<AnnotationDesc> annotations = new ArrayList<>(Arrays.asList(classDoc.annotations()));

        if (interfaceClass.isPresent()) {
            annotations.addAll(Arrays.asList(interfaceClass.get().annotations()));
        }

        return annotations.stream()
                .flatMap(a -> {
                    if (a.annotationType().qualifiedTypeName().equals(ApiUsecases.class.getName())) {
                        AnnotationValue[] values = JavaDocUtils.extractValue(a, "value");

                        return Stream.of(values).map(method -> (AnnotationDesc) method.value());
                    }
                    return Stream.of(a);
                })
                .filter(a -> a.annotationType().qualifiedTypeName().equals(ApiUsecase.class.getName()))
                .collect(Collectors.toList());
    }

}
