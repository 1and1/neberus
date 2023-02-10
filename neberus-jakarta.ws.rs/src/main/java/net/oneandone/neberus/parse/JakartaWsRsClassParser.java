package net.oneandone.neberus.parse;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;

import javax.lang.model.element.ExecutableElement;

import java.util.Collections;
import java.util.List;

import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;

/**
 * Parses class related things.
 */
public class JakartaWsRsClassParser extends ClassParser {

    public JakartaWsRsClassParser(JakartaWsRsMethodParser methodParser) {
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
