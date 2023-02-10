package net.oneandone.neberus.parse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import net.oneandone.neberus.Options;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import java.util.List;

import static net.oneandone.neberus.util.JavaDocUtils.getAnnotationValue;
import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;

/**
 * Parses all stuff related to a single REST method.
 */
public class JakartaWsRsMethodParser extends MethodParser {

    public JakartaWsRsMethodParser(Options options) {
        super(options);
    }

    @Override
    protected boolean skipParameter(ExecutableElement methodDoc, VariableElement parameter, int index) {
        return super.skipParameter(methodDoc, parameter, index)
               || hasAnnotation(methodDoc, parameter, Context.class, index, options.environment);
    }

    @Override
    protected String getPathParam(ExecutableElement method, VariableElement parameter, int index) {
        return getAnnotationValue(method, parameter, PathParam.class, VALUE, index, options.environment);
    }

    @Override
    protected String getQueryParam(ExecutableElement method, VariableElement parameter, int index) {
        return getAnnotationValue(method, parameter, QueryParam.class, VALUE, index, options.environment);
    }

    @Override
    protected String getHeaderParam(ExecutableElement method, VariableElement parameter, int index) {
        return getAnnotationValue(method, parameter, HeaderParam.class, VALUE, index, options.environment);
    }

    @Override
    protected String getFormParam(ExecutableElement method, VariableElement parameter, int index) {
        return getAnnotationValue(method, parameter, FormParam.class, VALUE, index, options.environment);
    }

    @Override
    protected String getRootPath(TypeElement classDoc) {
        return getAnnotationValue(classDoc, Path.class, VALUE, options.environment);
    }

    @Override
    protected String getPath(ExecutableElement methodDoc) {
        return getAnnotationValue(methodDoc, Path.class, VALUE, options.environment);
    }

    @Override
    protected List<AnnotationValue> getConsumes(ExecutableElement method) {
        return getAnnotationValue(method, Consumes.class, VALUE, options.environment);
    }

    @Override
    protected List<AnnotationValue> getProduces(ExecutableElement method) {
        return getAnnotationValue(method, Produces.class, VALUE, options.environment);
    }

}
