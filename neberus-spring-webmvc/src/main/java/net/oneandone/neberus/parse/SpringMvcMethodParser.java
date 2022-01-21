package net.oneandone.neberus.parse;

import net.oneandone.neberus.Options;
import net.oneandone.neberus.annotation.ApiFormParam;
import net.oneandone.neberus.util.MvcUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static net.oneandone.neberus.util.JavaDocUtils.extractValue;
import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;
import static net.oneandone.neberus.util.JavaDocUtils.typeCantBeDocumented;

/**
 * Parses all stuff related to a single REST method.
 */
public class SpringMvcMethodParser extends MethodParser {

    public static final String PATH = "path";
    public static final String NAME = "name";
    public static final String REQUIRED = "required";

    public SpringMvcMethodParser(Options options) {
        super(options);
    }

    @Override
    protected boolean skipParameter(ExecutableElement methodDoc, VariableElement parameter, int index) {
        return super.skipParameter(methodDoc, parameter, index)
                || parameter.asType().toString().equals("org.springframework.http.HttpHeaders")
                || parameter.asType().toString().contains("org.springframework.util.MultiValueMap")
                || !hasAnnotation(methodDoc, parameter, PathVariable.class, index, options.environment)
                && !hasAnnotation(methodDoc, parameter, RequestParam.class, index, options.environment)
                && !hasAnnotation(methodDoc, parameter, RequestBody.class, index, options.environment)
                && !hasAnnotation(methodDoc, parameter, RequestHeader.class, index, options.environment);
    }

    @Override
    protected String getPathParam(ExecutableElement method, VariableElement parameter, int index) {
        return getParam(method, parameter, index, PathVariable.class);
    }

    @Override
    protected String getQueryParam(ExecutableElement method, VariableElement parameter, int index) {
        return getParam(method, parameter, index, RequestParam.class);
    }

    @Override
    protected String getHeaderParam(ExecutableElement method, VariableElement parameter, int index) {
        return getParam(method, parameter, index, RequestHeader.class);
    }

    private String getParam(ExecutableElement method, VariableElement parameter, int index, Class<?> paramAnnotationClass) {
        if (hasAnnotation(method, parameter, paramAnnotationClass, index, options.environment)) {
            String value = getAnnotationValue(method, parameter, paramAnnotationClass, VALUE, index, options.environment);

            if (value != null) {
                return value;
            }

            String name = getAnnotationValue(method, parameter, paramAnnotationClass, NAME, index, options.environment);
            return name != null ? name : parameter.getSimpleName().toString();
        }
        return null;
    }

    @Override
    protected String getFormParam(ExecutableElement method, VariableElement parameter, int index) {
        if (hasAnnotation(method, parameter, ApiFormParam.class, index, options.environment)) {
            return getParam(method, parameter, index, RequestParam.class);
        }

        return null;
    }

    @Override
    protected String getRootPath(TypeElement classDoc) {
        if (MvcUtils.getMappingAnnotationValue(classDoc, PATH, options.environment) != null) {
            return (String) ((List<AnnotationValue>) MvcUtils.getMappingAnnotationValue(classDoc, PATH, options.environment)).get(0).getValue();
        }

        List<AnnotationValue> onClassValue = MvcUtils.getMappingAnnotationValue(classDoc, VALUE, options.environment);

        if (onClassValue != null) {
            return (String) onClassValue.get(0).getValue();
        }

        return "/";
    }

    @Override
    protected String getPath(ExecutableElement methodDoc) {
        if (MvcUtils.getMappingAnnotationValue(methodDoc, PATH, options.environment) != null) {
            return (String) ((List<AnnotationValue>) MvcUtils.getMappingAnnotationValue(methodDoc, PATH, options.environment)).get(0).getValue();
        }
        if (MvcUtils.getMappingAnnotationValue(methodDoc, VALUE, options.environment) != null) {
            return (String) ((List<AnnotationValue>) MvcUtils.getMappingAnnotationValue(methodDoc, VALUE, options.environment)).get(0).getValue();
        }

        return null;
    }

    @Override
    protected List<AnnotationValue> getConsumes(ExecutableElement method) {
        return MvcUtils.getMappingAnnotationValue(method, "consumes", options.environment);
    }

    @Override
    protected List<AnnotationValue> getProduces(ExecutableElement method) {
        return MvcUtils.getMappingAnnotationValue(method, "produces", options.environment);
    }

    @Override
    protected TypeMirror getResponseEntityClass(ExecutableElement method, AnnotationMirror response) {
        TypeMirror typeFromResponse = extractValue(response, "entityClass");
        if (typeFromResponse != null) {
            return typeFromResponse;
        } else {
            return typeCantBeDocumented(method.getReturnType(), options) ? null : method.getReturnType();
        }
    }

    @Override
    protected void addLabel(ExecutableElement method, RestMethodData data) {
        super.addLabel(method, data);
        if (data.methodData.label.equals(method.getSimpleName().toString())) {
            String mvcName = MvcUtils.getMappingAnnotationValue(method, NAME, options.environment);
            if (mvcName != null) {
                data.methodData.label = mvcName;
            }
        }
    }

    @Override
    protected RequiredStatus getRequiredStatus(ExecutableElement method, VariableElement parameter, int index) {
        RequiredStatus overrideValue = super.getRequiredStatus(method, parameter, index);

        if (overrideValue != RequiredStatus.UNSET) {
            return overrideValue;
        }

        Boolean required = null;

        if (hasAnnotation(method, parameter, PathVariable.class, index, options.environment)) {
            required = getAnnotationValue(method, parameter, PathVariable.class, REQUIRED, index, options.environment);
        } else if (hasAnnotation(method, parameter, RequestParam.class, index, options.environment)) {
            required = getAnnotationValue(method, parameter, RequestParam.class, REQUIRED, index, options.environment);
        } else if (hasAnnotation(method, parameter, RequestHeader.class, index, options.environment)) {
            required = getAnnotationValue(method, parameter, RequestHeader.class, REQUIRED, index, options.environment);
        }

        if (required != null) {
            return required ? RequiredStatus.REQUIRED : RequiredStatus.OPTIONAL;
        } else {
            return RequiredStatus.UNSET;
        }
    }

}
