package net.oneandone.neberus.util;

import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;

import jdk.javadoc.doclet.DocletEnvironment;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class MvcUtils {

    private MvcUtils() {
    }

    public static <T> T getMappingAnnotationValue(TypeElement classDoc, String key, DocletEnvironment environment) {
        if (hasAnnotation(classDoc, GetMapping.class, environment)) {
            return getAnnotationValue(classDoc, GetMapping.class, key, environment);
        } else if (hasAnnotation(classDoc, PutMapping.class, environment)) {
            return getAnnotationValue(classDoc, PutMapping.class, key, environment);
        } else if (hasAnnotation(classDoc, PostMapping.class, environment)) {
            return getAnnotationValue(classDoc, PostMapping.class, key, environment);
        } else if (hasAnnotation(classDoc, PatchMapping.class, environment)) {
            return getAnnotationValue(classDoc, PatchMapping.class, key, environment);
        } else if (hasAnnotation(classDoc, DeleteMapping.class, environment)) {
            return getAnnotationValue(classDoc, DeleteMapping.class, key, environment);
        } else {
            return getAnnotationValue(classDoc, RequestMapping.class, key, environment);
        }
    }

    public static <T> T getMappingAnnotationValue(ExecutableElement method, String key, DocletEnvironment environment) {
        if (hasAnnotation(method, GetMapping.class, environment)) {
            return getAnnotationValue(method, GetMapping.class, key, environment);
        } else if (hasAnnotation(method, PutMapping.class, environment)) {
            return getAnnotationValue(method, PutMapping.class, key, environment);
        } else if (hasAnnotation(method, PostMapping.class, environment)) {
            return getAnnotationValue(method, PostMapping.class, key, environment);
        } else if (hasAnnotation(method, PatchMapping.class, environment)) {
            return getAnnotationValue(method, PatchMapping.class, key, environment);
        } else if (hasAnnotation(method, DeleteMapping.class, environment)) {
            return getAnnotationValue(method, DeleteMapping.class, key, environment);
        } else {
            return getAnnotationValue(method, RequestMapping.class, key, environment);
        }
    }
}
