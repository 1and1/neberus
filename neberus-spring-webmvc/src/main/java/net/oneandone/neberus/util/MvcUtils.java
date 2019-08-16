package net.oneandone.neberus.util;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;

import org.springframework.web.bind.annotation.*;

public class MvcUtils {

    private MvcUtils() {
    }

    public static <T> T getMappingAnnotationValue(ClassDoc classDoc, String key) {
        if (hasAnnotation(classDoc, GetMapping.class)) {
            return getAnnotationValue(classDoc, GetMapping.class, key);
        } else if (hasAnnotation(classDoc, PutMapping.class)) {
            return getAnnotationValue(classDoc, PutMapping.class, key);
        } else if (hasAnnotation(classDoc, PostMapping.class)) {
            return getAnnotationValue(classDoc, PostMapping.class, key);
        } else if (hasAnnotation(classDoc, PatchMapping.class)) {
            return getAnnotationValue(classDoc, PatchMapping.class, key);
        } else if (hasAnnotation(classDoc, DeleteMapping.class)) {
            return getAnnotationValue(classDoc, DeleteMapping.class, key);
        } else {
            return getAnnotationValue(classDoc, RequestMapping.class, key);
        }
    }

    public static <T> T getMappingAnnotationValue(MethodDoc method, String key) {
        if (hasAnnotation(method, GetMapping.class)) {
            return getAnnotationValue(method, GetMapping.class, key);
        } else if (hasAnnotation(method, PutMapping.class)) {
            return getAnnotationValue(method, PutMapping.class, key);
        } else if (hasAnnotation(method, PostMapping.class)) {
            return getAnnotationValue(method, PostMapping.class, key);
        } else if (hasAnnotation(method, PatchMapping.class)) {
            return getAnnotationValue(method, PatchMapping.class, key);
        } else if (hasAnnotation(method, DeleteMapping.class)) {
            return getAnnotationValue(method, DeleteMapping.class, key);
        } else {
            return getAnnotationValue(method, RequestMapping.class, key);
        }
    }
}
