package net.oneandone.neberus.parse;

import static net.oneandone.neberus.parse.SpringMvcMethodParser.NAME;
import static net.oneandone.neberus.util.JavaDocUtils.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.oneandone.neberus.util.MvcUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Parses class related things.
 */
public class SpringMvcClassParser extends ClassParser {

    public SpringMvcClassParser(SpringMvcMethodParser methodParser) {
        super(methodParser);
    }

    @Override
    protected List<String> getHttpMethods(ExecutableElement method) {
        if (hasAnnotation(method, DeleteMapping.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.DELETE.name());
        } else if (hasAnnotation(method, GetMapping.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.GET.name());
        } else if (hasAnnotation(method, PostMapping.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.POST.name());
        } else if (hasAnnotation(method, PutMapping.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.PUT.name());
        } else if (hasAnnotation(method, PatchMapping.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.PATCH.name());
        } else if (hasAnnotation(method, RequestMapping.class, methodParser.options.environment)) {
            List<AnnotationValue> annotationValue = getAnnotationValue(method, RequestMapping.class, "method", methodParser.options.environment);
            if (annotationValue == null || annotationValue.isEmpty() || annotationValue.get(0) == null) {
                return Collections.emptyList();
            }
            return annotationValue.stream().map(v -> ((VariableElement) v.getValue()).getSimpleName().toString())
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    protected void addLabel(TypeElement classDoc, RestClassData data) {
        super.addLabel(classDoc, data);
        if (data.label.equals(classDoc.getSimpleName().toString())) {
            String mvcName = MvcUtils.getMappingAnnotationValue(classDoc, NAME, methodParser.options.environment);
            if (mvcName != null) {
                data.label = mvcName;
            }
        }
    }

}
