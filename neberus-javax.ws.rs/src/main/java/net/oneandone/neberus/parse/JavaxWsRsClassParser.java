package net.oneandone.neberus.parse;

import com.sun.javadoc.MethodDoc;

import static net.oneandone.neberus.util.JavaDocUtils.*;
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
    protected String getHttpMethod(MethodDoc method) {
        if (hasAnnotation(method, DELETE.class)) {
            return HttpMethod.DELETE;
        } else if (hasAnnotation(method, GET.class)) {
            return HttpMethod.GET;
        } else if (hasAnnotation(method, HEAD.class)) {
            return HttpMethod.HEAD;
        } else if (hasAnnotation(method, OPTIONS.class)) {
            return HttpMethod.OPTIONS;
        } else if (hasAnnotation(method, POST.class)) {
            return HttpMethod.POST;
        } else if (hasAnnotation(method, PUT.class)) {
            return HttpMethod.PUT;
        } else if (hasAnnotation(method, PATCH.class)) {
            return HttpMethod.PATCH;
        }
        return null;
    }

}
