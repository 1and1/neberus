package net.oneandone.neberus.parse;

import static net.oneandone.neberus.util.JavaDocUtils.*;

import javax.lang.model.element.ExecutableElement;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PATCH;

/**
 * Parses class related things.
 */
public class JavaxWsRsClassParser extends ClassParser {

    public JavaxWsRsClassParser(JavaxWsRsMethodParser methodParser) {
        super(methodParser);
    }

    @Override
    protected String getHttpMethod(ExecutableElement method) {
        if (hasAnnotation(method, DELETE.class, methodParser.options.environment)) {
            return HttpMethod.DELETE;
        } else if (hasAnnotation(method, GET.class, methodParser.options.environment)) {
            return HttpMethod.GET;
        } else if (hasAnnotation(method, HEAD.class, methodParser.options.environment)) {
            return HttpMethod.HEAD;
        } else if (hasAnnotation(method, OPTIONS.class, methodParser.options.environment)) {
            return HttpMethod.OPTIONS;
        } else if (hasAnnotation(method, POST.class, methodParser.options.environment)) {
            return HttpMethod.POST;
        } else if (hasAnnotation(method, PUT.class, methodParser.options.environment)) {
            return HttpMethod.PUT;
        } else if (hasAnnotation(method, PATCH.class, methodParser.options.environment)) {
            return HttpMethod.PATCH;
        }
        return null;
    }

}
