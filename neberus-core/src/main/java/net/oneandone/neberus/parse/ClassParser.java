package net.oneandone.neberus.parse;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiHeader;
import net.oneandone.neberus.annotation.ApiHeaders;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.util.JavaDocUtils;

import static net.oneandone.neberus.util.JavaDocUtils.*;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Parses class related things.
 */
public abstract class ClassParser {

    MethodParser methodParser;

    public ClassParser(MethodParser methodParser) {
        this.methodParser = methodParser;
    }

    public RestClassData parse(ClassDoc classDoc) {

        RestClassData restClassData = new RestClassData();

        // class related stuff
        addLabel(classDoc, restClassData);
        addHeaders(classDoc, restClassData);
        addDescription(classDoc, restClassData);

        restClassData.className = classDoc.name();
        restClassData.classDoc = classDoc;

        // contained methods
        MethodDoc[] methods = classDoc.methods();
        for (MethodDoc method : methods) {
            RestMethodData parsedMethodData = parseMethod(method);
            if (parsedMethodData != null) {
                parsedMethodData.containingClass = restClassData;
                restClassData.methods.add(parsedMethodData);
            }
        }

        return restClassData;
    }

    protected abstract String getHttpMethod(MethodDoc method);

    private RestMethodData parseMethod(MethodDoc method) {
        String httpMethod = getHttpMethod(method);

        if (httpMethod == null) {
            return null;
        }

        return methodParser.parseMethod(method, httpMethod);
    }

    /**
     * Use the value defined in {@link ApiLabel} or use the name of the class.
     */
    protected void addLabel(ClassDoc classDoc, RestClassData restClassData) {
        String label = getAnnotationValue(classDoc, ApiLabel.class, "value");
        if (label != null) {
            restClassData.label = label;
        } else {
            restClassData.label = classDoc.name();
        }
    }

    /**
     * Use the value defined in {@link ApiDescription} or use the javadoc comment of the class.
     */
    protected void addDescription(ClassDoc classDoc, RestClassData restClassData) {
        String description = getAnnotationValue(classDoc, ApiDescription.class, "value");

        if (description != null) {
            restClassData.description = description;
        } else {
            restClassData.description = getCommentText(classDoc);
        }
    }

    protected void addHeaders(ClassDoc classDoc, RestClassData restClassData) {
        AnnotationValue[] headers = getAnnotationValue(classDoc, ApiHeaders.class, "value");
        if (headers != null) {
            //more than one annotation is defined, so we got the container
            Stream.of(headers).forEach(header -> addHeader((AnnotationDesc) header.value(), restClassData));
        } else {
            //check if a single annotation is defined
            Optional<AnnotationDesc> singleResponse = getAnnotationDesc(classDoc, ApiHeader.class);
            if (singleResponse.isPresent()) {
                addHeader(singleResponse.get(), restClassData);
            }
        }
    }

    protected void addHeader(AnnotationDesc headerDesc, RestClassData restClassData) {
        String name = JavaDocUtils.extractValue(headerDesc, "name");
        String description = JavaDocUtils.extractValue(headerDesc, "description");

        RestMethodData.HeaderInfo headerInfo = new RestMethodData.HeaderInfo();

        headerInfo.name = name;
        headerInfo.description = description;

        restClassData.headerDefinitions.put(name, headerInfo);
    }

}
