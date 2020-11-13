package net.oneandone.neberus.parse;

import net.oneandone.neberus.Options;
import net.oneandone.neberus.ResponseType;
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

import static net.oneandone.neberus.util.JavaDocUtils.*;

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
                || !hasAnnotation(methodDoc, parameter, PathVariable.class, index, options.environment)
                && !hasAnnotation(methodDoc, parameter, RequestParam.class, index, options.environment)
                && !hasAnnotation(methodDoc, parameter, RequestBody.class, index, options.environment)
                && !hasAnnotation(methodDoc, parameter, RequestHeader.class, index, options.environment);
    }

    @Override
    protected String getPathParam(ExecutableElement method, VariableElement parameter, int index) {
        if (hasAnnotation(method, parameter, PathVariable.class, index, options.environment)) {
            String value = getAnnotationValue(method, parameter, PathVariable.class, VALUE, index, options.environment);
            return value != null ? value : parameter.getSimpleName().toString();
        }
        return null;

    }

    @Override
    protected String getQueryParam(ExecutableElement method, VariableElement parameter, int index) {
        if (hasAnnotation(method, parameter, RequestParam.class, index, options.environment)) {
            String value = getAnnotationValue(method, parameter, RequestParam.class, VALUE, index, options.environment);
            return value != null ? value : parameter.getSimpleName().toString();
        }
        return null;
    }

    @Override
    protected String getHeaderParam(ExecutableElement method, VariableElement parameter, int index) {
        if (hasAnnotation(method, parameter, RequestHeader.class, index, options.environment)) {
            String value = getAnnotationValue(method, parameter, RequestHeader.class, VALUE, index, options.environment);
            return value != null ? value : parameter.getSimpleName().toString();
        }
        return null;
    }

    @Override
    protected String getFormParam(ExecutableElement method, VariableElement parameter, int index) {
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
        return (String) ((List<AnnotationValue>) MvcUtils.getMappingAnnotationValue(methodDoc, VALUE, options.environment)).get(0).getValue();
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
    protected void addSuccessResponse(ExecutableElement method, AnnotationMirror response, RestMethodData data, List<AnnotationValue> produces) {

        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(ResponseType.SUCCESS);

        addCommonResponseData(method, response, responseData);

        TypeMirror typeFromResponse = extractValue(response, "entityClass");
        if (typeFromResponse != null) {
            responseData.entityClass = typeFromResponse;
        } else {
            responseData.entityClass = typeCantBeDocumented(method.getReturnType(), options) ? null : method.getReturnType();
        }

        addResponseData(response, data, produces, responseData);
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
    protected boolean isOptional(ExecutableElement method, VariableElement parameter, int index) {
        Boolean required = null;

        if (hasAnnotation(method, parameter, PathVariable.class, index, options.environment)) {
            required = getAnnotationValue(method, parameter, PathVariable.class, REQUIRED, index, options.environment);
        } else if (hasAnnotation(method, parameter, RequestParam.class, index, options.environment)) {
            required = getAnnotationValue(method, parameter, RequestParam.class, REQUIRED, index, options.environment);
        }

        if (required != null) {
            return !required;
        } else {
            return super.isOptional(method, parameter, index);
        }
    }

}
