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
import java.util.Collections;
import java.util.List;

/**
 * Parses class related things.
 */
public class JavaxWsRsClassParser extends ClassParser {

    public JavaxWsRsClassParser(JavaxWsRsMethodParser methodParser) {
        super(methodParser);
    }

    @Override
    protected List<String> getHttpMethods(ExecutableElement method) {
        if (hasAnnotation(method, DELETE.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.DELETE);
        } else if (hasAnnotation(method, GET.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.GET);
        } else if (hasAnnotation(method, HEAD.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.HEAD);
        } else if (hasAnnotation(method, OPTIONS.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.OPTIONS);
        } else if (hasAnnotation(method, POST.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.POST);
        } else if (hasAnnotation(method, PUT.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.PUT);
        } else if (hasAnnotation(method, PATCH.class, methodParser.options.environment)) {
            return Collections.singletonList(HttpMethod.PATCH);
        }
        return Collections.emptyList();
    }

}
