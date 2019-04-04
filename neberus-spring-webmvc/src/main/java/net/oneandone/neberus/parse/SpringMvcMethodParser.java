package net.oneandone.neberus.parse;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.ResponseType;
import static net.oneandone.neberus.util.JavaDocUtils.*;
import java.util.ArrayList;

import net.oneandone.neberus.util.MvcUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Parses all stuff related to a single REST method.
 */
public class SpringMvcMethodParser extends MethodParser {

    public static final String PATH = "path";
    public static final String NAME = "name";

    public SpringMvcMethodParser(Options options) {
        super(options);
    }

    @Override
    protected boolean skipParameter(MethodDoc methodDoc, Parameter parameter, int index) {
        return false;
    }

    @Override
    protected String getPathParam(MethodDoc method, Parameter parameter, int index) {
        if (hasAnnotation(method, parameter, PathVariable.class, index)) {
            String value = getAnnotationValue(method, parameter, PathVariable.class, VALUE, index);
            return value != null ? value : parameter.name();
        }
        return null;

    }

    @Override
    protected String getQueryParam(MethodDoc method, Parameter parameter, int index) {
        if (hasAnnotation(method, parameter, RequestParam.class, index)) {
            String value = getAnnotationValue(method, parameter, RequestParam.class, VALUE, index);
            return value != null ? value : parameter.name();
        }
        return null;
    }

    @Override
    protected String getHeaderParam(MethodDoc method, Parameter parameter, int index) {
        if (hasAnnotation(method, parameter, RequestHeader.class, index)) {
            String value = getAnnotationValue(method, parameter, RequestHeader.class, VALUE, index);
            return value != null ? value : parameter.name();
        }
        return null;
    }

    @Override
    protected String getRootPath(ClassDoc classDoc) {
        if (MvcUtils.getMappingAnnotationValue(classDoc, PATH) != null) {
            return (String) ((AnnotationValue[]) MvcUtils.getMappingAnnotationValue(classDoc, PATH))[0].value();
        }
        return (String) ((AnnotationValue[]) MvcUtils.getMappingAnnotationValue(classDoc, VALUE))[0].value();
    }

    @Override
    protected String getPath(MethodDoc methodDoc) {
        if (MvcUtils.getMappingAnnotationValue(methodDoc, PATH) != null) {
            return (String) ((AnnotationValue[]) MvcUtils.getMappingAnnotationValue(methodDoc, PATH))[0].value();
        }
        return (String) ((AnnotationValue[]) MvcUtils.getMappingAnnotationValue(methodDoc, VALUE))[0].value();
    }

    @Override
    protected AnnotationValue[] getConsumes(MethodDoc method) {
        return MvcUtils.getMappingAnnotationValue(method, "consumes");
    }

    @Override
    protected AnnotationValue[] getProduces(MethodDoc method) {
        return MvcUtils.getMappingAnnotationValue(method, "produces");
    }



    @Override
    protected void addSuccessResponse(MethodDoc method, AnnotationDesc response, RestMethodData data, AnnotationValue[] produces) {

        RestMethodData.ResponseData responseData = new RestMethodData.ResponseData(ResponseType.SUCCESS);

        addCommonResponseData(method, response, responseData);

        Type typeFromResponse = extractValue(response, "entityClass");
        if (typeFromResponse != null) {
            responseData.entityClass = typeFromResponse;
        } else {
            responseData.entityClass = typeCantBeDocumented(method.returnType(), options) ? null : method.returnType();
        }

        addResponseData(response, data, produces, responseData);
    }

    @Override
    protected void addLabel(MethodDoc method, RestMethodData data) {
        super.addLabel(method, data);
        if (data.methodData.label.equals(method.name())) {
            String mvcName = MvcUtils.getMappingAnnotationValue(method, NAME);
            if (mvcName != null) {
                data.methodData.label = mvcName;
            }
        }
    }

}
