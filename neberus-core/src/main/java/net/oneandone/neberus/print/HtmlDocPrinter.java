package net.oneandone.neberus.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Type;
import j2html.tags.ContainerTag;
import j2html.tags.Tag;
import net.oneandone.neberus.NeberusModule;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.model.CustomMediaType;
import net.oneandone.neberus.model.warning.WarningUrn;
import net.oneandone.neberus.parse.RestClassData;
import net.oneandone.neberus.parse.RestMethodData;
import net.oneandone.neberus.parse.RestUsecaseData;
import net.oneandone.neberus.shortcode.ShortCodeExpander;
import net.oneandone.neberus.util.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static j2html.TagCreator.*;
import static net.oneandone.neberus.parse.RestMethodData.ParameterType.*;
import static net.oneandone.neberus.parse.RestUsecaseData.getParameter;
import static net.oneandone.neberus.util.JavaDocUtils.*;

/**
 * Prints REST Documentation to HTML.
 */
//TODO replace 'toggleParams' logic with bootstrap collapse
public class HtmlDocPrinter extends DocPrinter {

    /**
     * used for unique references between "toggleParams" buttons and their related table rows
     */
    int toggleIdCounter = 0;

    private final ObjectMapper mapper;

    public HtmlDocPrinter(List<NeberusModule> modules, ShortCodeExpander expander, Options options) {
        super(modules, expander, options);
        this.mapper = new ObjectMapper();
    }

    protected void saveToHtmlFile(ContainerTag content, String fileName) {
        String doctype = "<!DOCTYPE html>\n";
        saveToFile(doctype + content.render(), options.outputDirectory + options.docBasePath, fileName + ".html");
    }

    public void printRestClassFile(RestClassData restClassData, List<RestClassData> allRestClasses,
                                   List<RestUsecaseData> restUsecases) {
        ContainerTag content = getRestClassContent(restClassData, allRestClasses, restUsecases);

        saveToHtmlFile(content, restClassData.className);
    }

    public void printIndexFile(List<RestClassData> restClasses, List<RestUsecaseData> restUsecases, String packageDoc) {
        ContainerTag content = getIndexFileContent(restClasses, restUsecases, packageDoc);
        saveToHtmlFile(content, "index");
    }

    protected ContainerTag getRestClassContent(RestClassData restClassData, List<RestClassData> allRestClasses,
                                               List<RestUsecaseData> restUsecases) {
        return html().with(
                getHead(restClassData.label),
                getBody(restClassData, allRestClasses, restUsecases)
        );
    }

    protected ContainerTag getHead(String title) {
        ContainerTag head = head();

        head = head.with(
                link().withRel("stylesheet").withHref("lib/css/bootstrap.min.css"),
                link().withRel("stylesheet").withHref("lib/css/bootstrap-theme.min.css").attr("title", "dark"),
                link().withRel("alternate stylesheet").withHref("lib/css/bootstrap-theme-light.min.css").attr("title", "light"),
                link().withRel("stylesheet").withHref("lib/css/jquery.tocify.css"),
                link().withRel("stylesheet").withHref("css/neberus.css"),
                link().withRel("stylesheet").withHref("css/neberus-dark.css").attr("title", "dark"),
                link().withRel("alternate stylesheet").withHref("css/neberus-light.css").attr("title", "light")
        );

        for (String listFile : FileUtils.listFiles(HtmlDocPrinter.class.getResource("/modules/css"))) {
            head = head.with(link().withRel("stylesheet").withHref("modules/css/" + listFile));
        }

        head = head.with(
                script().withSrc("lib/js/jQuery.js").withType("text/javascript"),
                script().withSrc("lib/js/jquery-ui.min.js").withType("text/javascript"),
                script().withSrc("lib/js/bootstrap.min.js").withType("text/javascript"),
                script().withSrc("lib/js/jquery.tocify.min.js").withType("text/javascript"),
                script().withSrc("js/neberus.js").withType("text/javascript")
        );

        for (String listFile : FileUtils.listFiles(HtmlDocPrinter.class.getResource("/modules/js"))) {
            head = head.with(script().withSrc("modules/js/" + listFile).withType("text/javascript"));
        }

        head = head.with(
                meta().withCharset("utf-8"),
                title(title)
        );

        return head;
    }

    private ContainerTag getBody(RestClassData restClassData, List<RestClassData> allRestClasses,
                                 List<RestUsecaseData> restUsecases) {
        return body().with(getThemeSwitcher(),
                getToc(),
                div().withClass("serviceContainer").with(
                        h1(restClassData.label),
                        getClassDescription(restClassData),
                        getTableOfContent(restClassData.methods)
                ).with(
                        getRestMethods(restClassData, allRestClasses, restUsecases)
                ).with(
                        getCredits()
                )
        );
    }

    protected ContainerTag getCredits() {
        return div().withClass("credits").with(
                rawHtml("Proudly presented by "),
                a("Neberus").withHref("https://github.com/1and1/neberus")
        );
    }

    protected ContainerTag getToc() {
        return div().withClass("navigationContainer").with(
                div().withClass("tocify overviewLink").with(
                        a(options.apiTitle + " " + options.apiVersion).withHref("index.html")
                ),
                div().withClass("tocify options").with(
                        getTocOptions()
                ),
                div().withId("toc")
        );
    }

    protected ContainerTag getThemeSwitcher() {
        return div().withClass("choose-style").with(
                span("Theme: "),
                span("light").withClass("choose-style-item").attr("onclick", "setActiveStyleSheet('light')"),
                rawHtml(" | "),
                span("dark").withClass("choose-style-item").attr("onclick", "setActiveStyleSheet('dark')")
        );
    }

    private ContainerTag getClassDescription(RestClassData restClassData) {
        if (StringUtils.isBlank(restClassData.description)) {
            return div();
        }

        return div().withClass("panel panel-primary").with(
                div().withClass("panel-body").with(
                        rawHtml(expander.expand(restClassData.description))
                )
        );
    }

    private ContainerTag getTableOfContent(List<RestMethodData> restMethods) {
        return div().withClass("panel panel-primary").with(
                div().withClass("panel-body path-toc").with(
                        ul().with(
                                getTableOfContentEntries(restMethods)
                        ).withClass("path-toc-list")
                )
        );
    }

    private List<ContainerTag> getTableOfContentEntries(List<RestMethodData> restMethods) {
        int padTo = restMethods.stream().map(method -> method.methodData.httpMethod).distinct()
                .sorted((a, b) -> Integer.compare(b.length(), a.length())).map(String::length).findFirst().orElse(0);

        return restMethods.stream().sorted(Comparator.comparing(a -> a.methodData.httpMethod))
                .sorted(Comparator.comparing(a -> a.methodData.path)).map(method -> {

                    String path = method.methodData.path
                            .replaceAll("//+", "/")
                            .replaceAll("/", "/<span class='word-wrap'></span>");

                    ContainerTag divEntry = div().with(
                            div().with(
                                    div().withClass("path-toc-method").with(
                                            rawHtml(padRight(method.methodData.httpMethod, padTo) + " - ")
                                    ),
                                    div().withClass("path-toc-path").with(
                                            rawHtml(path)
                                    )
                            )
                    );

                    return li().withClass(method.methodData.deprecated ? "path-toc-deprecated" : "").with(
                            a().attr("location", (method.methodData.httpMethod + "-" + method.methodData.label).replace(" ", ""))
                                    .withHref("#" + printMethodId(method.methodData))
                                    .with(
                                            getTooltipLeft(
                                                    method.methodData.label
                                                            + (needsDeprecatedLabel(method.methodData) ? " - DEPRECATED" : ""),
                                                    divEntry.toString()
                                            )
                                    )
                    );
                }).collect(Collectors.toList());
    }

    private List<ContainerTag> getRestMethods(RestClassData restClassData, List<RestClassData> allRestClasses,
                                              List<RestUsecaseData> restUsecases) {
        return restClassData.methods.stream().map(method -> {
                    RestMethodData.MethodData methodData = method.methodData;

                    ContainerTag headingDiv = div().withClass("headingContainer").with(
                            h2(methodData.httpMethod + " - " + methodData.label
                                    + (needsDeprecatedLabel(methodData) ? " - DEPRECATED" : ""))
                                    .withId(printMethodId(methodData)).withClass("methodHeading")
                    );

                    ContainerTag methodDiv = div().withClass("well").with(headingDiv);

                    List<RestUsecaseData.UsecaseData> relatedUsecases = restUsecases.stream()
                            .flatMap(u -> u.usecases.stream())
                            .filter(usecase -> usecase.methods.stream()
                                    .filter(usecaseMethodData -> usecaseMethodData.linkedMethod != null)
                                    .anyMatch(usecaseMethodData -> usecaseMethodData.linkedMethod.equals(method)))
                            .collect(Collectors.toList());

                    // reference usecases
                    if (!relatedUsecases.isEmpty()) {

                        StringJoiner sj = new StringJoiner("<br>");

                        relatedUsecases.forEach(usecase -> {
                            sj.add(a(usecase.name)
                                    .withHref("index.html#" + usecase.name.replace(" ", ""))
                                    .toString()
                            );
                        });

                        headingDiv.with(
                                getPopover(sj.toString(), "Related Usecases", "bottom")
                                        .withClass("btn btn-primary relatedUsecasesToggle")
                        );
                    }

                    if (methodData.deprecated) {
                        //deprecated warning
                        methodDiv.with(getDeprecatedWarning(method, allRestClasses));
                    }

                    methodDiv.with(getMethodPath(method));

                    if (!StringUtils.isBlank(methodData.description)) {
                        methodDiv.with( //method description
                                div().withClass("methodDescription").with(
                                        rawHtml(expander.expand(methodData.description))
                                ));
                    }

                    methodDiv.with(getRequestData(method.requestData, methodData));
                    methodDiv.with(getResponseData(method, restClassData.headerDefinitions));

                    if (methodData.deprecated) {
                        return div().withClass("deprecated").with(methodDiv);
                    } else {
                        return methodDiv;
                    }
                }
        ).collect(Collectors.toList());
    }

    private boolean needsDeprecatedLabel(RestMethodData.MethodData methodData) {
        return methodData.deprecated && !methodData.label.contains("DEPRECATED");
    }

    private ContainerTag getRequestData(RestMethodData.RequestData requestData, RestMethodData.MethodData methodData) {
        ContainerTag requestDiv = div().withClass("panel panel-primary table-responsive").with(
                div().withClass("panel-heading").with(rawHtml("Request")));

        if (methodData.printCurl) {
            requestDiv.with(div().withClass("panel panel-default").with(
                    div().withClass("panel-heading").with(rawHtml("Curl")),
                    div().withClass("panel-body").with(
                            getCode(printCurl(requestData, methodData, new HashMap<>())) // allow word-wrap before ampersand
                    )
            ));
        }

        //print content-type, if present
        if (requestData.mediaType != null && !requestData.mediaType.isEmpty()) {
            requestDiv.with(div().withClass("panel panel-default").with(
                    div().withClass("panel-heading").with(rawHtml("ContentType")),
                    div().withClass("panel-body").with(
                            div().withClass("code").with(rawHtml(requestData.mediaType.toString()))
                    )
            ));
        }

        if (!requestData.parameters.isEmpty()) {
            ContainerTag table = table().withClass("table table-striped parameters").with(
                    tr().with(th("Name"), th("Type"), th("Entity"), th("Description"), th("Allowed Values"))
            );

            requestDiv.with(div().withClass("panel panel-default").with(div().withClass("panel-heading").with(
                    rawHtml("Parameters")),
                    table
            ));

            requestData.parameters.forEach(parameter -> {
                int toggleId = ++toggleIdCounter;

                boolean hasNestedParameters = !parameter.nestedParameters.isEmpty();

                table.with(getParameterRow(hasNestedParameters, toggleId, parameter, requestData, methodData));

                if (hasNestedParameters) {
                    table.with(getNestedParameters(parameter.nestedParameters, toggleId + "", requestData, methodData,
                            null, 2));
                }
            });

        }

        return requestDiv;
    }

    private ContainerTag getCode(String content) {
        return div().withClass("code").with(rawHtml(content
                .replace(",", ",<span class='word-wrap'></span>")
                .replace("&", "<span class='word-wrap'></span>&")
                .replace("?", "<span class='word-wrap'></span>?")
                .replaceAll("([a-zA-Z}>])/([a-zA-Z{<])", "$1/<span class='word-wrap'></span>$2")
        ));
    }

    private ContainerTag getMethodPath(RestMethodData method) {

        String path = method.methodData.path;

        for (RestMethodData.ParameterInfo parameter : method.requestData.parameters) {
            if (parameter.parameterType == PATH) {
                path = path.replace("{" + parameter.name + "}", printWithParameterReference(parameter.name, PATH,
                        method.methodData, false, "{" + parameter.name + "}"));
            }
        }

        return //method path
                div().withClass("panel panel-primary").with(
                        div().withClass("panel-body").with(
                                div().withClass("code").with(rawHtml(path.replaceAll("//+", "/")))
                        )
                );
    }

    private ContainerTag getDeprecatedWarning(RestMethodData restMethodData, List<RestClassData> allRestClasses) {
        ContainerTag deprecated = div().withClass("panel panel-warning deprecated-description").with(
                div().withClass("panel-heading").with(rawHtml("Deprecated"))
        );

        if (StringUtils.isNoneBlank(restMethodData.methodData.deprecatedDescription)) {
            deprecated.with(
                    div().withClass("panel-body").with(
                            rawHtml(getDeprecatedMethodDescription(restMethodData, allRestClasses))
                    )
            );
        }

        return deprecated;
    }

    private String getDeprecatedMethodDescription(RestMethodData restMethodData, List<RestClassData> allRestClasses) {
        String description = expander.expand(restMethodData.methodData.deprecatedDescription);

        if (!restMethodData.methodData.deprecatedLinks.isEmpty()) {

            for (MethodDoc link : restMethodData.methodData.deprecatedLinks) {

                for (RestClassData restClass : allRestClasses) {
                    Optional<RestMethodData> linkedMethod = restClass.methods.stream()
                            .filter(m -> m.methodData.methodDoc.compareTo(link) == 0).findFirst();

                    if (linkedMethod.isPresent()) {
                        String replacement;

                        replacement = getLinkToMethod(restMethodData.containingClass, restClass, linkedMethod.get());
                        description = description.replaceFirst("\\{\\@link.*[\\r\\n]*.*\\}", replacement);
                    }
                }
            }
        }

        return description;
    }

    private String getLinkToMethod(RestClassData thisRestClass, RestClassData linkedRestClass, RestMethodData linkedMethod) {
        String replacement;
        if (linkedRestClass.equals(thisRestClass)) { // in same class
            replacement = "<a location=\""
                    + (linkedMethod.methodData.httpMethod + " - " + linkedMethod.methodData.label)
                    .replace(" ", "")
                    + "\" href=\"#" + printMethodId(linkedMethod.methodData) + "\">"
                    + linkedMethod.methodData.httpMethod + " - " + linkedMethod.methodData.label
                    + "</a>";
        } else {
            replacement = "<a href=\""
                    + linkedRestClass.className + ".html#"
                    + (linkedMethod.methodData.httpMethod + " - " + linkedMethod.methodData.label).replace(" ", "")
                    + (needsDeprecatedLabel(linkedMethod.methodData) ? "-DEPRECATED" : "")
                    + "\">"
                    + linkedMethod.methodData.httpMethod + " - " + linkedMethod.methodData.label
                    + "</a>";
        }
        return replacement;
    }

    private ContainerTag getParameterRow(boolean hasNestedParameters, int toggleId, RestMethodData.ParameterInfo parameter,
                                         RestMethodData.RequestData requestData, RestMethodData.MethodData methodData) {
        ContainerTag row = tr()
                .withName(getParameterReference(parameter.name, parameter.parameterType, methodData.label))
                .withClass("parameterHighlight");

        if (hasNestedParameters) {
            //add required classes, if this cell should act as "button" to collapse the related params
            row.withClass("toggleParams").withHref(toggleId + "").withData("toggle-id", toggleId + "")
                    .withData("container", "body")
                    .withData("placement", "left")
                    .withData("toggle", "tooltip")
                    .withData("trigger", "hover")
                    .attr("title", "Click to show contained rows")
                    .with(td().with(
                            rawHtml(parameter.name),
                            span().withClass("glyphicon-toggle glyphicon glyphicon-menu-right glyphicon-" + toggleId)
                    ));
        } else {
            row.with(td(parameter.name));
        }

        row.with(td(parameter.parameterType.name().toLowerCase()));

        if (parameter.parameterType == BODY && parameter.entityClass != null) {
            row.with(td().with(getPopover(prettyPrintJson(toTemplate(requestData.mediaType.get(0),
                    parameter.entityClass, new HashMap<>(), methodData, true)),
                    getSimpleTypeName(parameter.entityClass))));

        } else if (parameter.displayClass != null) {
            row.with(td().withText(getSimpleTypeName(parameter.displayClass)));
        } else {
            row.with(td());
        }

        row.with(
                td(rawHtml(expander.expand(parameter.description))),
                td(parameter.allowedValues)
        );

        return row;
    }

    private List<ContainerTag> getNestedParameters(List<RestMethodData.ParameterInfo> nestedParameters, String toggleId,
                                                   RestMethodData.RequestData requestData, RestMethodData.MethodData methodData,
                                                   String parent, int layer) {
        return nestedParameters.stream().flatMap(p -> {
            List<ContainerTag> rows = new ArrayList<>();

            boolean hasNestedParameters = !p.nestedParameters.isEmpty();

            String subToggleId = toggleId + "-" + ++toggleIdCounter;

            ContainerTag row = tr().withClasses("collapse out " + toggleId, "parameterHighlight")
                    .withName(getParameterReference(concat(parent, p.name), BODY, methodData.label));

            ContainerTag nameCell = td().withStyle("padding-left: " + layer * 25 + "px");

            if (hasNestedParameters) {
                //add required classes, if this cell should act as "button" to collapse the related params
                row.withClasses("toggleParams", "collapse out " + toggleId).withHref(subToggleId)
                        .withData("toggle-id", subToggleId)
                        .withData("toggle-parent", toggleId)
                        .withData("container", "body")
                        .withData("placement", "left")
                        .withData("toggle", "tooltip")
                        .withData("trigger", "hover")
                        .attr("title", "Click to show contained rows")
                        .with(nameCell.with(
                                rawHtml(p.name),
                                span().withClass("glyphicon-toggle glyphicon glyphicon-menu-right glyphicon-" + subToggleId)
                        ));
            } else {
                row.with(nameCell.with(rawHtml(p.name))).withData("toggle-parent", toggleId);
            }

            row.with(td()); // empty cell for 'type'

            if (p.entityClass != null) {
                row.with(td().with(getPopover(prettyPrintJson(toTemplate(requestData.mediaType.get(0),
                        p.entityClass, new HashMap<>(), methodData, true)),
                        getSimpleTypeName(p.entityClass))));
            } else {
                row.with(td());
            }

            row.with(td(rawHtml(expander.expand(p.description))));
            row.with(td(p.allowedValues));

            rows.add(row);

            if (hasNestedParameters) {
                rows.addAll(getNestedParameters(p.nestedParameters, subToggleId, requestData, methodData,
                        concat(parent, p.name), layer + 1));
            }
            return rows.stream();
        }).collect(Collectors.toList());
    }

    private String printCurl(RestMethodData.RequestData requestData, RestMethodData.MethodData methodData,
                             Map<String, String> parameterOverrides) {

        if (methodData.curl != null) {
            //use the provided curl, if present
            return methodData.curl;
        }

        //or generate the curl
        StringBuilder sb = new StringBuilder();
        //HTTP method
        sb.append("curl -i -X ").append(methodData.httpMethod).append(" ");

        //Content-Type
        if (requestData.mediaType != null) {
            sb.append("-H 'Content-Type: ").append(requestData.mediaType.get(0)).append(";charset=utf-8' ");
        }

        //request headers
        if (requestData.parameters != null && !requestData.parameters.isEmpty()) {
            requestData.parameters.stream()
                    .filter(parameter -> parameter.parameterType == HEADER && parameter.entityClass != null)
                    .filter(parameter -> !parameter.name.equals("Content-Type"))
                    .filter(p -> !parameterOverrides.containsKey(p.name) || parameterOverrides.get(p.name) != null)
                    .forEach(parameter -> sb
                            .append(printWithParameterReference(parameter.name, parameter.parameterType, methodData, false,
                                    "-H '" + parameter.name + ": "
                                            + parameterOverrides.getOrDefault(parameter.name, "{String}")
                                            + "'"))
                            .append(" "));
        }

        //request body
        if (requestData.parameters != null && !requestData.parameters.isEmpty()) {
            requestData.parameters.stream()
                    .filter(parameter -> parameter.parameterType == BODY && parameter.entityClass != null).findFirst()
                    .ifPresent(entity -> sb.append("-d '")
                            .append(toTemplate(requestData.mediaType.get(0), entity.entityClass, parameterOverrides, methodData, false))
                            .append("' "));
        }

        //URL with known placeholder replaced by default values
        sb.append("'<span class='curlHost'>");
        if (!options.apiHost.startsWith("http://") && !options.apiHost.startsWith("https://")) { // add http:// if missing
            sb.append("http://");
        }
        sb.append(options.apiHost).append("</span>/").append(options.apiBasePath).append("/")
                .append(replacePlaceholderWithAllowedValuesInPath(methodData.path, requestData, methodData, parameterOverrides));

        //query parameters, filled with defaults where possible
        if (requestData.parameters != null && !requestData.parameters.isEmpty()) {

            StringJoiner sj = new StringJoiner("&");
            requestData.parameters.stream().filter(parameter -> parameter.parameterType == QUERY)
                    .filter(p -> !parameterOverrides.containsKey(p.name) || parameterOverrides.get(p.name) != null)
                    .forEach(parameter -> {
                        sj.add(printWithParameterReference(parameter.name, parameter.parameterType, methodData, false,
                                parameter.name + "=" + parameterOverrides.getOrDefault(parameter.name,
                                        "{" + getParameterType(parameter) + "}")));
                    });
            if (sj.length() != 0) {
                sb.append("?").append(sj.toString());
            }
        }

        //replace multiple slashes with single slash where needed
        return sb.append("'").toString().replaceAll("//+", "/")
                .replaceAll("http:/", "http://").replaceAll("https:/", "https://"); //TODO simplify replacements
    }

    private String printWithParameterReference(String name, RestMethodData.ParameterType type, RestMethodData.MethodData method,
                                               boolean skip, String value) {
        if (skip) {
            return value;
        }
        return span().withName(getParameterReference(name, type, method.label)).withClass("parameterHighlight")
                .with(rawHtml(value)).toString().replace("\"", "'");
    }

    private String concat(String... s) {
        return concat(Arrays.asList(s));
    }

    private String concat(List<String> strings) {
        StringJoiner sj = new StringJoiner(".");
        strings.stream().filter(Objects::nonNull).filter(StringUtils::isNotEmpty).forEach(sj::add);
        return sj.toString();
    }

    private String getParameterReference(String name, RestMethodData.ParameterType type, String method) {
        String sanitizedMethod = method.replaceAll("[/!\"§$%&\\\\()=?#*+~\\[\\]{}]", "");
        return (sanitizedMethod + "-" + type.name() + "-" + name).replace(" ", "-");
    }

    private String replacePlaceholderWithAllowedValuesInPath(String path, RestMethodData.RequestData requestData,
                                                             RestMethodData.MethodData methodData,
                                                             Map<String, String> parameterOverrides) {
        String newPath = path;
        for (Map.Entry<String, String> parameter : parameterOverrides.entrySet()) {
            if (parameter.getValue() == null) {
                continue;
            }
            if (newPath.contains("{" + parameter.getKey() + "}")) {
                newPath = newPath.replace("{" + parameter.getKey() + "}",
                        printWithParameterReference(parameter.getKey(), PATH, methodData, false, parameter.getValue()));
            }
        }
        for (RestMethodData.ParameterInfo parameter : requestData.parameters) {
            if (parameter.parameterType == PATH && newPath.contains("{" + parameter.name + "}")) {
                newPath = newPath.replace("{" + parameter.name + "}",
                        printWithParameterReference(parameter.name, parameter.parameterType, methodData, false,
                                "{" + getParameterType(parameter) + "}"));
            }
        }
        return newPath;
    }

    private List<ContainerTag> getNestedResponseValues(List<RestMethodData.ParameterInfo> nestedParameters,
                                                       RestMethodData.MethodData methodData, String toggleId, int layer) {
        return nestedParameters.stream().flatMap(p -> {
            List<ContainerTag> rows = new ArrayList<>();

            boolean hasNestedParameters = !p.nestedParameters.isEmpty();

            String subToggleId = toggleId + "-" + ++toggleIdCounter;

            ContainerTag row = tr().withClass("collapse out " + toggleId);

            ContainerTag nameCell = td().withStyle("padding-left: " + layer * 25 + "px");

            if (hasNestedParameters) {
                //add required classes, if this cell should act as "button" to collapse the related params
                row.withClasses("toggleParams", "collapse out " + toggleId).withHref(subToggleId)
                        .withData("container", "body")
                        .withData("placement", "left")
                        .withData("toggle", "tooltip")
                        .withData("trigger", "hover")
                        .attr("title", "Click to show contained rows")
                        .with(nameCell.with(
                                rawHtml(p.name),
                                span().withClass("glyphicon-toggle glyphicon glyphicon-menu-right glyphicon-" + subToggleId)
                        ));
            } else {
                row.with(nameCell.with(rawHtml(p.name)));
            }

            // entity
            if (p.entityClass != null) {
                row.with(td().with(getPopover(prettyPrintJson(toJsonTemplate(p.entityClass, new HashMap<>(), p.name, methodData, true)),
                        getSimpleTypeName(p.entityClass))));
            } else {
                row.with(td());
            }

            row.with(
                    td(rawHtml(expander.expand(p.description))),
                    td(), // content type
                    td() // headers
            );

            if (!StringUtils.isBlank(p.allowedValues)) {
                row.with(td(p.allowedValues));
            } else {
                row.with(td());
            }

            rows.add(row);

            if (hasNestedParameters) {
                rows.addAll(getNestedResponseValues(p.nestedParameters, methodData, subToggleId, layer + 1));
            }

            return rows.stream();
        }).collect(Collectors.toList());
    }

    private ContainerTag getResponseData(RestMethodData restMethodData, Map<String, RestMethodData.HeaderInfo> headerInfos) {

        ContainerTag responseDiv = div().withClass("panel panel-primary table-responsive").with(
                div().withClass("panel-heading").with(rawHtml("Response"))
        );

        ContainerTag tableContainer = responseDiv;

        if (!restMethodData.responseValues.isEmpty()) {
            tableContainer = div().withClass("panel panel-default").with(
                    div().withClass("panel-heading").with(rawHtml("Responses"))
            );
            responseDiv.with(
                    getUnrelatedResponseValues(restMethodData.responseValues),
                    tableContainer
            );
        }

        ContainerTag table = table().withClass("table table-striped").with(
                tr().with(th("Status"), th("Entity"), th("Description"), th("Content-Type"), th("Headers"), th("Value"))
        );

        restMethodData.responseData.forEach(response -> {
            int toggleId = ++toggleIdCounter;

            boolean hasRelatedResponseValues = !response.nestedParameters.isEmpty();

            table.with(getResponseRow(response, hasRelatedResponseValues, restMethodData.methodData, toggleId, headerInfos));

            if (hasRelatedResponseValues) {
                table.with(getNestedResponseValues(response.nestedParameters, restMethodData.methodData,
                        String.valueOf(toggleId), 2));
            }

        });

        tableContainer.with(table);

        return responseDiv;
    }

    private ContainerTag getResponseRow(RestMethodData.ResponseData response, boolean hasRelatedResponseValues,
                                        RestMethodData.MethodData methodData,
                                        int toggleId, Map<String, RestMethodData.HeaderInfo> headerInfos) {

        ContainerTag row = tr().withClass(getTableRowClass(response));

        if (hasRelatedResponseValues) {
            //add required classes, if this cell should act as "button" to collapse the related params
            row.withClasses("toggleParams", getTableRowClass(response)).withHref(toggleId + "")
                    .withData("container", "body")
                    .withData("placement", "left")
                    .withData("toggle", "tooltip")
                    .withData("trigger", "hover")
                    .attr("title", "Click to show contained rows")
                    .with(td().with(
                            rawHtml(response.status.value + " " + response.status.reasonPhrase),
                            span().withClass("glyphicon-toggle glyphicon glyphicon-menu-right glyphicon-" + toggleId)
                    ));
        } else {
            row.with(td().with(
                    rawHtml(response.status.value + " " + response.status.reasonPhrase)
            ));
        }

        if (response.entityClass != null) {
            row.with(td().with(
                    getPopover(prettyPrintJson(toJsonTemplate(response.entityClass, new HashMap<>(),
                            response.entityClass.simpleTypeName(), methodData, true)), response.entityClass.asClassDoc()
                            .simpleTypeName())
            ));
        } else if (response.problem != null) {
            row.with(td().with(
                    getPopover(prettyPrintJson(toProblemTemplate(response.problem)), "Problem")
            ));
        } else if (!response.warnings.isEmpty()) {
            row.with(td().with(
                    getPopover(prettyPrintJson(toWarningTemplate(response.warnings)), "Warning")
            ));
        } else {
            row.with(td());
        }

        row.with(
                td(rawHtml(expander.expand(response.description))),
                td(getContentType(response)),
                td().with(getHeaders(response.headers, headerInfos)),
                td()
        );

        return row;
    }

    private ContainerTag getUnrelatedResponseValues(List<RestMethodData.ParameterInfo> unmappedCustomParameters) {

        ContainerTag table = table().withClass("table table-striped").with(
                tr().with(th("Name"), th("Type"), th("Description"), th("Allowed Values"))
        );

        unmappedCustomParameters.forEach(rv -> {
            table.with(tr().with(
                    td(rv.name),
                    td(rv.parameterType.name().toLowerCase()),
                    td(rawHtml(expander.expand(rv.description))),
                    td(rv.allowedValues)
            ));
        });

        return div().withClass("panel panel-default").with(
                div().withClass("panel-heading").with(rawHtml("Response Values")),
                table
        );
    }

    private String getContentType(RestMethodData.ResponseData responseData) {
        if (responseData.problem != null) {
            return CustomMediaType.APPLICATION_PROBLEM_JSON;
        } else if (!responseData.warnings.isEmpty()) {
            return CustomMediaType.APPLICATION_WARNING_JSON;
        } else {
            return responseData.contentType;
        }
    }

    private String prettyPrintJson(String jsonString) {
        try {
            Object json = mapper.readValue(jsonString, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (IOException ex) {
            //not an json string, so return unmodified
            return jsonString;
        }
    }

    private List<Tag> getHeaders(List<RestMethodData.HeaderInfo> headerInfos,
                                 Map<String, RestMethodData.HeaderInfo> globalHeaderInfos) {

        return headerInfos.stream().flatMap(header -> {
            List<Tag> tags = new ArrayList<>();

            String description = header.description;

            if (description == null) {
                //search for a description in the headers defined on the class
                RestMethodData.HeaderInfo info = globalHeaderInfos.get(header.name);
                if (info != null && info.description != null) {
                    description = info.description;
                }
            }

            description = expander.expand(description);

            tags.add(getTooltipLabel(description, header.name));
            tags.add(br());

            return tags.stream();
        }).collect(Collectors.toList());
    }

    private ContainerTag getTooltipLabel(String content, String title) {
        return span().withClass("label label-default headerLabel")
                .attr("data-container", "body")
                .attr("data-toggle", "tooltip")
                .attr("data-placement", "top")
                .attr("title", content)
                .with(rawHtml(title));
    }

    private ContainerTag getTooltipTop(String content, String title) {
        return getTooltip(content, title, "top");
    }

    private ContainerTag getTooltipLeft(String content, String title) {
        return getTooltip(content, title, "left");
    }

    private ContainerTag getTooltip(String content, String title, String placement) {
        return span().attr("data-container", "body")
                .attr("data-toggle", "tooltip")
                .attr("data-placement", placement)
                .attr("title", content)
                .with(rawHtml(title));
    }

    protected ContainerTag getPopover(String content, String title) {
        return getPopover(content, title, "top");
    }

    private ContainerTag getPopover(String content, String title, String placement) {
        return button().withClass("btn btn-primary")
                .attr("data-container", "body")
                .attr("data-toggle", "popover")
                .attr("data-placement", placement)
                .attr("data-html", "true")
                .attr("data-content", "<pre>" + content + "</pre>")
                .with(rawHtml(title));
    }

    private String getTableRowClass(RestMethodData.ResponseData responseData) {
        switch (responseData.reponseType) {
            case SUCCESS:
                return "success";
            case WARNING:
                return "warning";
            case PROBLEM:
                return "danger";
            case GENERIC:
                return "generic";
            default:
                return "";
        }
    }

    private String toTemplate(String mediaType, Type type, Map<String, String> parameterOverrides,
                              RestMethodData.MethodData methodData, boolean skipEnhance) {
        if (type.asClassDoc().isEnum()) {
            return toEnumTemplate(type);
        }

        if (mediaType.equals("application/x-www-form-urlencoded")) {
            return toFormUrlEncodedTemplate(type, parameterOverrides, null, methodData, skipEnhance);
        } else {
            return toJsonTemplate(type, parameterOverrides, null, methodData, skipEnhance);
        }
    }

    protected String toEnumTemplate(Type type) {
        StringBuilder sb = new StringBuilder();
        Stream.of(type.asClassDoc().enumConstants()).sorted().forEach(ec -> sb.append(ec.name()).append("\n"));
        return sb.toString();
    }

    //TODO refactor problem response with entity?
    private String toProblemTemplate(RestMethodData.ProblemInfo problemInfo) {
        ObjectNode objectNode = mapper.createObjectNode();

        objectNode.put("type", "urn:problem:ui." + problemInfo.type.value);
        objectNode.put("title", problemInfo.title);
        objectNode.put("detail", problemInfo.detail);

        return objectNode.toString();
    }

    //TODO refactor warning response with entity?
    private String toWarningTemplate(List<RestMethodData.ProblemInfo> warnings) {
        ArrayNode arrayNode = mapper.createArrayNode();

        warnings.forEach(warning -> {
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("type", WarningUrn.PREFIX + warning.type.value);
            objectNode.put("title", warning.title);
            objectNode.put("detail", warning.detail);
            arrayNode.add(objectNode);
        });
        return arrayNode.toString();
    }

    private String toJsonTemplate(Type type, Map<String, String> paramterOverrides, String parent,
                                  RestMethodData.MethodData methodData, boolean skipEnhance) {
        JsonNode jsonNode = toJsonNode(type, paramterOverrides, parent, methodData, skipEnhance);
        try {
            String jsonString = mapper.writer(new MinimalPrettyPrinter()).writeValueAsString(jsonNode);
            jsonString = jsonString.replaceAll("\"(<[^\"]+>)([^\"^<]+)(<\\/span>)\":((?:\"[^\"]+\")|(?:\\[(?:[^\\]\\[]+|\\[(?:[^\\]\\[]+|\\[[^\\]\\[]*\\])*\\])*\\])|(?:\\{(?:[^\\}\\{]+|\\{(?:[^\\}\\{]+|\\{[^\\}\\{]*\\})*\\})*\\}))",
                    "$1\"$2\":$4$3");
            return jsonString;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private JsonNode toJsonNode(Type type, Map<String, String> paramterOverrides, String parent,
                                RestMethodData.MethodData methodData, boolean skipEnhance) {

        if (isArrayType(type)) {
            return processArrayType(type, paramterOverrides, parent, methodData, skipEnhance);
        } else if (isMapType(type)) {
            return processMapType(type, paramterOverrides, parent, methodData, skipEnhance);
        } else {
            ObjectNode dataTable = mapper.createObjectNode();

            Map<String, Type> dataFields = getDataFields(type);

            dataFields.entrySet().stream()
                    .filter(e -> !paramterOverrides.containsKey(e.getKey()) || paramterOverrides.get(e.getKey()) != null)
                    .forEach(e -> {
                        if (isArrayType(e.getValue())) {
                            dataTable.put(printWithParameterReference(concat(parent, e.getKey()), BODY, methodData, skipEnhance, e.getKey()),
                                    processArrayType(e.getValue(), paramterOverrides, parent, methodData, skipEnhance));
                        } else if (isMapType(e.getValue())) {
                            dataTable.put(printWithParameterReference(concat(parent, e.getKey()), BODY, methodData, skipEnhance, e.getKey()),
                                    processMapType(e.getValue(), paramterOverrides, parent, methodData, skipEnhance));
                        } else if (containedFieldNamesAreNotAvailableOrPackageExcluded(e.getValue(), options) // stop at 'arg0' etc. this does not provide useful information
                                || e.getValue().equals(type)) {  // break simple recursive loops
                            dataTable.put(printWithParameterReference(concat(parent, e.getKey()), BODY, methodData, skipEnhance, e.getKey()),
                                    paramterOverrides.getOrDefault(concat(parent, e.getKey()), "{" + getTypeString(e.getValue()) + "}"));
                        } else {
                            dataTable.setAll(processType(e.getValue(), e.getKey(), paramterOverrides, concat(parent), methodData, skipEnhance));
                        }

                    });

            return dataTable;
        }
    }

    private ObjectNode processType(Type type, String fieldName, Map<String, String> parameterOverrides, String parent,
                                   RestMethodData.MethodData methodData, boolean skipEnhance) {

        ObjectNode node = mapper.createObjectNode();

        if (fieldName != null
                && type.asClassDoc() != null
                && !type.isPrimitive()
                && !type.qualifiedTypeName().startsWith("java.lang")
                && !type.asClassDoc().isEnum()
                && type.asClassDoc().fields(false).length != 0) {
            node.replace(fieldName, toJsonNode(type, parameterOverrides, concat(parent, fieldName), methodData, skipEnhance));
        } else if (isMapType(type)) {
            ObjectNode mapNode = processMapType(type, parameterOverrides, concat(parent, fieldName), methodData, skipEnhance);

            if (fieldName != null) {
                node.replace(fieldName, mapNode);
            } else {
                node.setAll(mapNode);
            }
        } else if (isArrayType(type)) {
            ArrayNode arrayNode = processArrayType(type, parameterOverrides, concat(parent, fieldName), methodData, skipEnhance);

            if (fieldName != null) {
                node.replace(fieldName, arrayNode);
            } else {
                throw new IllegalArgumentException("field name is required for array type");
            }

        } else if (fieldName != null) {
            node.put(printWithParameterReference(concat(parent, fieldName), BODY, methodData, skipEnhance, fieldName),
                    "{" + getTypeString(type) + "}");
        }

        return node;
    }

    private ObjectNode processMapType(Type type, Map<String, String> paramterOverrides, String parent,
                                      RestMethodData.MethodData methodData, boolean skipEnhance) {
        ObjectNode mapNode = mapper.createObjectNode();
        Type[] typeArguments = type.asParameterizedType().typeArguments();

        if (isArrayType(typeArguments[1])) {
            mapNode.put("{" + getTypeString(typeArguments[0]) + "}",
                    processArrayType(typeArguments[1], paramterOverrides, parent, methodData, skipEnhance));
        } else if (typeArguments[1] != null && typeArguments[1].asClassDoc() != null && !typeArguments[1].isPrimitive()
                && !typeArguments[1].qualifiedTypeName().startsWith("java.lang") && !typeArguments[1].asClassDoc().isEnum()) {
            mapNode.put("{" + getTypeString(typeArguments[0]) + "}",
                    processType(typeArguments[1], null, paramterOverrides, parent, methodData, skipEnhance));
        } else {
            mapNode.put("{" + getTypeString(typeArguments[0]) + "}", "{" + getTypeString(typeArguments[1]) + "}");
        }

        return mapNode;
    }

    private ArrayNode processArrayType(Type type, Map<String, String> paramterOverrides, String parent,
                                       RestMethodData.MethodData methodData, boolean skipEnhance) {

        ArrayNode arrayNode = mapper.createArrayNode();
        Type[] typeArguments = type.asParameterizedType().typeArguments();

        if (isArrayType(typeArguments[0])) {
            arrayNode.add(processArrayType(typeArguments[0], paramterOverrides, parent, methodData, skipEnhance));
        } else if (isMapType(typeArguments[0])) {
            arrayNode.add(processMapType(typeArguments[0], paramterOverrides, parent, methodData, skipEnhance));
        } else if (typeArguments[0] != null && typeArguments[0].asClassDoc() != null && !typeArguments[0].isPrimitive()
                && !typeArguments[0].qualifiedTypeName().startsWith("java.lang") && !typeArguments[0].asClassDoc().isEnum()) {
            arrayNode.add(toJsonNode(typeArguments[0], paramterOverrides, parent, methodData, skipEnhance));
        } else {
            arrayNode.add("{" + getTypeString(typeArguments[0]) + "}");
        }

        return arrayNode;
    }

    private String getParameterType(RestMethodData.ParameterInfo parameterInfo) {
        if (parameterInfo.displayClass == null) {
            return getTypeString(parameterInfo.entityClass);
        } else {
            return getTypeString(parameterInfo.displayClass);
        }
    }

    private String toFormUrlEncodedTemplate(Type type, Map<String, String> paramterOverrides, String parent,
                                            RestMethodData.MethodData methodData, boolean skipEnhance) {

        if (isArrayType(type)) {
            return ""; // TODO find representation
        } else if (isMapType(type)) {
            return ""; // TODO find representation
        } else {
            StringJoiner sj = new StringJoiner("&");

            Map<String, Type> dataFields = getDataFields(type);

            dataFields.entrySet().stream()
                    .filter(e -> !paramterOverrides.containsKey(concat(parent, e.getKey()))
                            || paramterOverrides.get(concat(parent, e.getKey())) != null)
                    .forEach(e -> {
                        StringBuilder fb = new StringBuilder();
                        fb.append(printWithParameterReference(concat(parent, e.getKey()), BODY, methodData, skipEnhance,
                                e.getKey() + "=" + paramterOverrides.getOrDefault(concat(parent, e.getKey()),
                                        "{" + getTypeString(e.getValue()) + "}")));
                        sj.add(fb.toString());
                    });

            return sj.toString();
        }
    }

    private String padRight(String s, int length) {
        String padded = s;

        while (padded.length() < length) {
            padded += " ";
        }

        return padded;
    }

    private String printMethodId(RestMethodData.MethodData methodData) {
        return methodData.label.replace(" ", "") + methodData.methodDoc.hashCode();
    }

    protected ContainerTag getIndexFileContent(List<RestClassData> restClasses, List<RestUsecaseData> restUsecases, String packageDoc) {
        return html().with(
                getHead(options.apiTitle + " " + options.apiVersion),
                getIndexBody(restClasses, restUsecases, packageDoc)
        );
    }

    private ContainerTag getIndexBody(List<RestClassData> restClasses, List<RestUsecaseData> restUsecases, String packageDoc) {
        ContainerTag navigationContainer = div().withClass("navigationContainer").with(
                div().withId("toc").withStyle("top:" + (restClasses.size() * 40 + 20) + "px"),
                div().withClass("tocify overviewLink").with(
                        getIndexReferences(restClasses)
                )
        );

        modules.stream().filter(module -> StringUtils.isNotBlank(module.getFilename()))
                .forEach(module -> navigationContainer.with(div().withClass("tocify overviewLink").with(getModuleLink(module))));

        return body().with(
                getThemeSwitcher(),
                navigationContainer,
                getIndexMainPage(packageDoc, options.apiTitle + " " + options.apiVersion, restUsecases),
                getCredits()
        );
    }

    private ContainerTag getTocOptions() {
        return form().withClass("form-inline").with(
                div().withClass("form-group w-100").with(
                        div().withClass("input-group w-100").with(
                                input().withClass("w-100").withId("filter").withPlaceholder("Search"),
                                span().withId("filterReset").withClass("glyphicon glyphicon-remove form-control-feedback")
                        )
                )
                // TODO implement ordering
//                div().withClass("form-group w-50").with(
//                        select().withClass("form-control w-100").withId("order").with(
//                                option("Default Order").withValue("default"),
//                                option("Order by Method").withValue("method"),
//                                option("Order By Name").withValue("name")
//                        )
//                )
        );
    }

    private ContainerTag getIndexReferences(List<RestClassData> restClasses) {
        ContainerTag ul = ul().withClass("tocify-header nav nav-list");
        restClasses.forEach(c -> ul.with(li().withClass("tocify-item").with(a(c.label).withHref(c.className + ".html"))));
        return ul;
    }

    private ContainerTag getModuleLink(NeberusModule module) {
        return ul().withClass("tocify-header nav nav-list").with(
                li().withClass("tocify-item").with(a(module.getName()).withHref(module.getFilename()))
        );
    }

    private ContainerTag getIndexMainPage(String packageDoc, String title, List<RestUsecaseData> restUsecases) {
        ContainerTag div = div().withClass("serviceContainer");

        if (!StringUtils.isBlank(title) && (StringUtils.isBlank(packageDoc) || !packageDoc.contains("<h1>"))) {
            div.with(h1(title));
        }

        if (!StringUtils.isBlank(packageDoc)) {
            div.with(div().withClass("well").with(rawHtml(packageDoc)));
        }

        if (!restUsecases.isEmpty()) {
            div.with(getUsecases(restUsecases));
        }

        return div;
    }

    private ContainerTag getUsecases(List<RestUsecaseData> restUsecases) {
        ContainerTag div = div().with(h2("Usecases"));

        restUsecases.forEach(restUsecasesData -> {
            div.with(getUsecases(restUsecasesData));
        });

        return div().withClass("well").with(div);
    }

    private ContainerTag getUsecases(RestUsecaseData restUsecasesData) {
        if (restUsecasesData.usecases.isEmpty()) {
            return div();
        }

        return div().with(
                div().withClass("well").with(
                        span(rawHtml(restUsecasesData.description == null ? "" : expander.expand(restUsecasesData.description)))
                )
        ).with(getUsecaseEntries(restUsecasesData.usecases));
    }

    private List<ContainerTag> getUsecaseEntries(List<RestUsecaseData.UsecaseData> usecases) {
        return usecases.stream().map(usecase -> {
                    int toggleId = ++toggleIdCounter;
                    return div().withClass("panel panel-primary").with(
                            div().withClass("panel-heading collapseToggle collapsed")
                                    .withData("toggle", "collapse")
                                    .withData("target", "#toggle" + toggleId)
                                    .with(
                                            h3().withClass("panel-title").with(
                                                    a(usecase.name)
                                            )
                                    ),
                            div().withClass("panel-collapse collapse")
                                    .withId("toggle" + toggleId)
                                    // height is changed by jquery, set to 0 initially
                                    .withStyle("height: 0px;")
                                    .with(
                                            div().withClass("panel-body")
                                                    .with(
                                                            span(rawHtml(expander.expand(usecase.description)))
                                                    )
                                            , getUsecaseMethodEntries(usecase.methods)
                                    )
                    );
                }
        ).collect(Collectors.toList());
    }

    private ContainerTag getUsecaseMethodEntries(List<RestUsecaseData.UsecaseMethodData> usecaseMethods) {
        ContainerTag table = table().withClass("table table-striped").with(
                tr().with(
                        th("Description"),
                        th("Method"),
                        th("Parameters"),
                        th("Response Value")
                ));

        usecaseMethods.forEach(method -> {

            String name;
            if (method.linkedMethod != null) {
                name = getLinkToMethod(null, method.linkedMethod.containingClass, method.linkedMethod);
            } else {
                name = expander.expand(method.name);
            }

            table.with(
                    tr().with(
                            td(rawHtml(method.description == null ? "" : expander.expand(method.description))),
                            td(rawHtml(name)).withClass(method.linkedMethod != null && method.linkedMethod.methodData.deprecated
                                                        ? "path-toc-deprecated" : ""),
                            td().with(getUsecaseMethodParameters(method)),
                            td().with(getUsecaseMethodResponseValue(method.responseValue))
                    ));

            if (method.linkedMethod != null) {
                table.with(
                        tr().with(
                                td().attr("colspan", "4").with(
                                        div().withClass("panel panel-small").with(
                                                getUsecaseMethodPath(method)
                                        ),
                                        div().withClass("panel panel-small").with(
                                                getCode(printCurlForUsecaseMethod(method))
                                        )
                                )
                        ));
            }

        });


        return table;
    }

    private ContainerTag getUsecaseMethodPath(RestUsecaseData.UsecaseMethodData method) {

        String path = method.linkedMethod.methodData.path;

        for (Map.Entry<String, RestUsecaseData.UsecaseValueInfo> entry : method.parameters.entrySet()) {
            path = path.replace("{" + entry.getKey() + "}", printWithParameterReference(entry.getKey(), PATH,
                    method.linkedMethod.methodData, false,
                    entry.getValue().value != null && !entry.getValue().value.equals("")
                    ? entry.getValue().value
                    : "{" + entry.getKey() + "}"));
        }

        return div().withClass("code").with(
                rawHtml(method.linkedMethod.methodData.httpMethod + " " + path.replaceAll("//+", "/"))
        );
    }

    private String printCurlForUsecaseMethod(RestUsecaseData.UsecaseMethodData method) {

        HashMap<String, String> parameterOverrides = new HashMap<>();

        Set<String> usedParams = new HashSet<>();

        method.parameters.keySet().forEach(k -> {
            usedParams.add(k);

            Optional<RestMethodData.ParameterInfo> linkedParam = getParameter(method.linkedMethod.requestData.parameters, k);

            if (linkedParam.isPresent() && linkedParam.get().parameterType == BODY && k.contains(".")) {
                String head = null;
                String tail;

                do {
                    String[] split = k.split("\\.", 2);
                    head = concat(head, split[0]);
                    tail = split[1];
                    usedParams.add(head);
                } while (tail.contains("."));
            }
        });

        // set null for !body params that should be omitted
        method.linkedMethod.requestData.parameters.stream()
                .filter(p -> p.parameterType != BODY)
                .filter(p -> !usedParams.contains(p.name))
                .forEach(p -> parameterOverrides.put(p.name, null));

        // set null for body params that should be omitted
        method.linkedMethod.requestData.parameters.stream()
                .filter(p -> p.parameterType == BODY)
                .flatMap(p -> getDataFields(p.entityClass).entrySet().stream())
                .filter(f -> !usedParams.contains(f.getKey()))
                .forEach(f -> parameterOverrides.put(f.getKey(), null));

        // set actual values
        parameterOverrides.putAll(method.parameters.entrySet().stream()
                .filter(e -> e.getValue().value != null && !e.getValue().value.equals(""))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().value)));

        return printCurl(method.linkedMethod.requestData, method.linkedMethod.methodData, parameterOverrides);
    }

    private ContainerTag getUsecaseMethodParameters(RestUsecaseData.UsecaseMethodData method) {
        if (method.parameters.isEmpty()) {
            return div();
        }

        ContainerTag table = table().withClass("table table-striped table-condensed");

        method.parameters.forEach((key, value) -> {

            Optional<RestMethodData.ParameterInfo> linkedParam = Optional.empty();
            String methodData = method.name;

            if (method.linkedMethod != null) {
                linkedParam = getParameter(method.linkedMethod.requestData.parameters, key);
                methodData = method.linkedMethod.methodData.label;
            }

            table.with(
                    tr().withName(linkedParam.isPresent()
                                  ? getParameterReference(key, linkedParam.get().parameterType, methodData)
                                  : "")
                            .withClass("parameterHighlight").with(
                            td().with(
                                    addDescriptionTooltip(
                                            span().with(
                                                    span(key),
                                                    span(!linkedParam.isPresent() || linkedParam.get().parameterType == null
                                                         ? ""
                                                         : " (" + linkedParam.get().parameterType.name().toLowerCase() + ")")
                                                            .withClass("italic valueHint noselect")
                                            ), linkedParam)
                            ),
                            td(value.value == null || value.value.equals("")
                               ? span(value.valueHint == null ? "" : value.valueHint).withClass("valueHint noselect")
                               : value.valueHint == null ? span(value.value) : getTooltipTop(value.valueHint, value.value))
                    ));
        });

        return table;
    }

    private ContainerTag addDescriptionTooltip(ContainerTag tr, Optional<RestMethodData.ParameterInfo> linkedParam) {
        if (linkedParam.isPresent() && linkedParam.get().description != null && !linkedParam.get().description.equals("")) {
            tr.attr("data-container", "body")
                    .attr("data-toggle", "tooltip")
                    .attr("data-placement", "top")
                    .attr("title", linkedParam.get().description);
        }

        return tr;
    }

    private ContainerTag getUsecaseMethodResponseValue(Map<String, RestUsecaseData.UsecaseValueInfo> responseValue) {
        if (responseValue.isEmpty()) {
            return div();
        }

        ContainerTag table = table().withClass("table table-striped table-condensed");

        responseValue.forEach((key, value) -> table.with(
                tr().with(
                        td(key),
                        td(value.value == null || value.value.equals("")
                           ? span(value.valueHint == null ? "" : value.valueHint).withClass("valueHint noselect")
                           : value.valueHint == null ? span(value.value) : getTooltipTop(value.valueHint, value.value))
                )));

        return table;
    }


}
