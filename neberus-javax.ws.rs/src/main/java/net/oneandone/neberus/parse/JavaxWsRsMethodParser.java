package net.oneandone.neberus.parse;

import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import net.oneandone.neberus.Options;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import static net.oneandone.neberus.util.JavaDocUtils.*;

/**
 * Parses all stuff related to a single REST method.
 */
public class JavaxWsRsMethodParser extends MethodParser {

    public JavaxWsRsMethodParser(Options options) {
        super(options);
    }

    @Override
    protected boolean skipParameter(MethodDoc methodDoc, Parameter parameter, int index) {
        return hasAnnotation(methodDoc, parameter, Context.class, index);
    }

    @Override
    protected String getPathParam(MethodDoc method, Parameter parameter, int index) {
        return getAnnotationValue(method, parameter, PathParam.class, VALUE, index);
    }

    @Override
    protected String getQueryParam(MethodDoc method, Parameter parameter, int index) {
        return getAnnotationValue(method, parameter, QueryParam.class, VALUE, index);
    }

    @Override
    protected String getHeaderParam(MethodDoc method, Parameter parameter, int index) {
        return getAnnotationValue(method, parameter, HeaderParam.class, VALUE, index);
    }

    @Override
    protected String getFormParam(MethodDoc method, Parameter parameter, int index) {
        return getAnnotationValue(method, parameter, FormParam.class, VALUE, index);
    }

    @Override
    protected String getRootPath(ClassDoc classDoc) {
        return getAnnotationValue(classDoc, Path.class, VALUE);
    }

    @Override
    protected String getPath(MethodDoc methodDoc) {
        return getAnnotationValue(methodDoc, Path.class, VALUE);
    }

    @Override
    protected AnnotationValue[] getConsumes(MethodDoc method) {
        return getAnnotationValue(method, Consumes.class, VALUE);
    }

    @Override
    protected AnnotationValue[] getProduces(MethodDoc method) {
        return getAnnotationValue(method, Produces.class, VALUE);
    }

}
