package net.oneandone.neberus.parse;

import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import static net.oneandone.neberus.parse.SpringMvcMethodParser.NAME;
import static net.oneandone.neberus.util.JavaDocUtils.*;
import java.util.StringJoiner;
import java.util.stream.Stream;

import net.oneandone.neberus.util.MvcUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Parses class related things.
 */
public class SpringMvcClassParser extends ClassParser {

    public SpringMvcClassParser(SpringMvcMethodParser methodParser) {
        super(methodParser);
    }

    @Override
    protected String getHttpMethod(MethodDoc method) {
        if (hasAnnotation(method, DeleteMapping.class)) {
            return HttpMethod.DELETE.name();
        } else if (hasAnnotation(method, GetMapping.class)) {
            return HttpMethod.GET.name();
        } else if (hasAnnotation(method, PostMapping.class)) {
            return HttpMethod.POST.name();
        } else if (hasAnnotation(method, PutMapping.class)) {
            return HttpMethod.PUT.name();
        } else if (hasAnnotation(method, PatchMapping.class)) {
            return HttpMethod.PATCH.name();
        } else if (hasAnnotation(method, RequestMapping.class)) {
            AnnotationValue[] annotationValue = getAnnotationValue(method, RequestMapping.class, "method");
            if (annotationValue == null || annotationValue.length == 0 || annotationValue[0] == null) {
                return null;
            }
            StringJoiner sj = new StringJoiner(" | ");
            Stream.of(annotationValue).forEach(v -> sj.add(((((FieldDoc) v.value()).name()))));
            return sj.toString();
        }

        return null;
    }

    @Override
    protected void addLabel(ClassDoc classDoc, RestClassData data) {
        super.addLabel(classDoc, data);
        if (data.label.equals(classDoc.name())) {
            String mvcName = MvcUtils.getMappingAnnotationValue(classDoc, NAME);
            if (mvcName != null) {
                data.label = mvcName;
            }
        }
    }

}
