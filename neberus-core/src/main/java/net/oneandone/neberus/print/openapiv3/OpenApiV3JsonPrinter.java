package net.oneandone.neberus.print.openapiv3;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import net.oneandone.neberus.NeberusModule;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.model.CookieSameSite;
import net.oneandone.neberus.parse.RestClassData;
import net.oneandone.neberus.parse.RestMethodData;
import net.oneandone.neberus.parse.RestUsecaseData;
import net.oneandone.neberus.print.AsciiDocPrinter;
import net.oneandone.neberus.print.DocPrinter;
import net.oneandone.neberus.print.MarkdownPrinter;
import net.oneandone.neberus.shortcode.ShortCodeExpander;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.oneandone.neberus.util.JavaDocUtils.asElement;
import static net.oneandone.neberus.util.JavaDocUtils.containedFieldNamesAreNotAvailableOrPackageExcluded;
import static net.oneandone.neberus.util.JavaDocUtils.getEnumValuesAsList;
import static net.oneandone.neberus.util.JavaDocUtils.getOpenApiTypeString;
import static net.oneandone.neberus.util.JavaDocUtils.getQualifiedName;
import static net.oneandone.neberus.util.JavaDocUtils.getSimpleTypeName;
import static net.oneandone.neberus.util.JavaDocUtils.isCollectionType;
import static net.oneandone.neberus.util.JavaDocUtils.isEnum;
import static net.oneandone.neberus.util.JavaDocUtils.isMapType;
import static net.oneandone.neberus.util.JavaDocUtils.typeCantBeDocumented;

@SuppressWarnings("PMD.TooManyStaticImports")
public class OpenApiV3JsonPrinter extends DocPrinter {

    private final ObjectMapper mapper;
    private final MarkdownPrinter markdownPrinter = new MarkdownPrinter();
    private final AsciiDocPrinter asciiDocPrinter = new AsciiDocPrinter();

    public OpenApiV3JsonPrinter(List<NeberusModule> modules, ShortCodeExpander expander, Options options) {
        super(modules, expander, options);
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void printRestClassFile(RestClassData restClassData, List<RestClassData> allRestClasses,
            List<RestUsecaseData> restUsecases) {
        //noop
    }

    @Override
    public void printIndexFile(List<RestClassData> restClasses, List<RestUsecaseData> restUsecases, String packageDoc) {
        OpenAPI openAPI = new OpenAPI();

        Components components = new Components();

        openAPI.info(getInfo(packageDoc))
                .paths(getPaths(restClasses, restUsecases, components))
                .components(components)
                .servers(getServers());

        openAPI.addExtension("x-resources-metadata", getResourcesMetadata(restClasses));
        openAPI.addExtension("x-usecases", getUsecasesExtension(restUsecases));

        try {
            String jsonString = mapper.writeValueAsString(openAPI);

            saveToFile(jsonString, options.outputDirectory + options.docBasePath, "openApi.json");

            String escapedJsonString = jsonString.replaceAll("\\\\", "\\\\\\\\").replaceAll("`", "\\\\`");
            String jsonVar = "var openApiJsonString = `" + escapedJsonString + "`;";
            saveToFile(jsonVar, options.outputDirectory + options.docBasePath, "openApi.js");
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }
    }

    private List<Server> getServers() {
        List<Server> servers = new ArrayList<>();

        options.apiHosts.forEach(host -> {
            String[] split = host.trim().split("\\[");

            Server server = new Server();

            String url = split[0];

            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            if (!options.apiBasePath.isBlank()) {
                String basePath = options.apiBasePath;
                String divider = basePath.startsWith("/") ? "" : "/";
                if (basePath.endsWith("/")) {
                    basePath = basePath.substring(0, basePath.length() - 1);
                }
                url += divider + basePath;
            }

            server.url(url);

            if (split.length > 1) {
                server.description(split[1].substring(0, split[1].length() - 1));
            }

            servers.add(server);
        });

        return servers;
    }

    private Map<String, Map<String, String>> getResourcesMetadata(List<RestClassData> restClasses) {
        return restClasses.stream().collect(Collectors.toMap(clazz -> clazz.className, clazz -> {
            HashMap<String, String> metadata = new HashMap<>();
            metadata.put("description", expand(clazz.description));
            metadata.put("shortDescription", expand(clazz.shortDescription));
            metadata.put("label", clazz.label);
            return metadata;
        }));
    }

    private String expand(String description) {
        if (StringUtils.isBlank(description)) {
            return description;
        }

        String htmlDescription;

        switch (options.markup) {
            case MARKDOWN:
                htmlDescription = markdownPrinter.print(description);
                break;
            case ASCIIDOC:
                htmlDescription = asciiDocPrinter.print(description);
                break;
            case HTML:
            default:
                htmlDescription = description;
                break;
        }

        String htmlReplacedDescription = htmlDescription.replaceAll("[^<br>](\n\n)", "$0<br>");

        return expander.expand(htmlReplacedDescription);
    }

    private Paths getPaths(List<RestClassData> restClasses, List<RestUsecaseData> restUsecases, Components components) {
        Paths paths = new Paths();

        restClasses.forEach(restClassData -> addRestClass(restClassData, restUsecases, paths, restClasses, components));
        return paths;
    }

    private Info getInfo(String packageDoc) {
        Info info = new Info();

        info.description(expand(packageDoc))
                .title(options.apiTitle)
                .version(options.apiVersion);

        info.addExtension("x-generated-at", Instant.now().toString());
        info.addExtension("x-generated-by", "https://github.com/1and1/neberus");
        info.addExtension("x-generated-by-version", getClass().getPackage().getImplementationVersion());

        return info;
    }

    private HashMap<String, Object> getUsecasesExtension(List<RestUsecaseData> restUsecases) {

        if (restUsecases.isEmpty()) {
            return null;
        }

        HashMap<String, Object> usecaseOverview = new HashMap<>();
        restUsecases.forEach(restUsecaseData -> {

            usecaseOverview.put("description", expand(restUsecaseData.description));

            Map<String, Object> usecases = new HashMap<>();
            usecaseOverview.put("usecases", usecases);

            restUsecaseData.usecases.forEach(usecaseData -> {

                HashMap<String, Object> usecase = new HashMap<>();
                usecases.put(usecaseData.id, usecase);

                usecase.put("name", usecaseData.name);
                usecase.put("description", expand(usecaseData.description));

                LinkedList<Object> methods = new LinkedList<>();
                usecase.put("methods", methods);


                usecaseData.methods.forEach(usecaseMethodData -> {
                    HashMap<String, Object> method = new HashMap<>();
                    methods.add(method);

                    method.put("path", usecaseMethodData.path);
                    method.put("httpMethod", usecaseMethodData.httpMethod);
                    method.put("description", expand(usecaseMethodData.description));

                    Map<String, HashMap<String, String>> params = toStringMap(usecaseMethodData.parameters);
                    method.put("parameters", params);

                    Map<String, HashMap<String, String>> requestBody = toStringMap(usecaseMethodData.requestBody);
                    method.put("requestBody", requestBody);

                    Map<String, HashMap<String, String>> responseBody = toStringMap(usecaseMethodData.responseBody);
                    method.put("responseBody", responseBody);

                    RestMethodData linkedMethod = usecaseMethodData.linkedMethod;
                    if (linkedMethod != null) {
                        HashMap<String, String> linkedMethodMap = getLinkedMethodMap(linkedMethod);
                        method.put("linkedMethod", linkedMethodMap);
                    }

                });
            });


        });

        return usecaseOverview;
    }

    private HashMap<String, String> getLinkedMethodMap(RestMethodData linkedMethod) {
        HashMap<String, String> linkedMethodMap = new HashMap<>();
        linkedMethodMap.put("operationId", getOperationId(linkedMethod));
        linkedMethodMap.put("resource", linkedMethod.containingClass.className);
        linkedMethodMap.put("label", linkedMethod.methodData.label);
        linkedMethodMap.put("httpMethod", linkedMethod.methodData.httpMethod);
        return linkedMethodMap;
    }

    private String getOperationId(RestMethodData linkedMethod) {
        return linkedMethod.methodData.httpMethod.toUpperCase() + "-" + linkedMethod.methodData.label.replaceAll("[^A-Za-z0-9]", "_");
    }

    private Map<String, HashMap<String, String>> toStringMap(Map<String, RestUsecaseData.UsecaseValueInfo> valueInfoMap) {
        return valueInfoMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            HashMap<String, String> param = new HashMap<>();
            param.put("value", e.getValue().value);
            param.put("valueHint", e.getValue().valueHint);
            return param;
        }));
    }

    private void addRestClass(RestClassData restClassData, List<RestUsecaseData> restUsecases, Paths paths, List<RestClassData> allRestClasses,
            Components components) {
        restClassData.methods.stream().collect(Collectors.groupingBy(e -> e.methodData.path))
                .forEach((path, methods) -> {
                    PathItem pathItem;

                    // path may have already been added by another resource for different httpMethods
                    if (paths.containsKey(path)) {
                        pathItem = paths.get(path);
                    } else {
                        pathItem = new PathItem();
                        paths.addPathItem(path, pathItem);
                    }

                    methods.forEach(method -> {
                        pathItem.operation(PathItem.HttpMethod.valueOf(method.methodData.httpMethod),
                                getOperation(restClassData, restUsecases, method, allRestClasses, components));
                    });
                });
    }

    private Operation getOperation(RestClassData restClassData, List<RestUsecaseData> restUsecases, RestMethodData method, List<RestClassData> allRestClasses,
            Components components) {
        Operation operation = new Operation();

        operation.operationId(getOperationId(method))
                .summary(method.methodData.label)
                .description(expand(method.methodData.description))
                .parameters(getParameterItems(restClassData, method.requestData.parameters, method.methodData, components))
                .deprecated(method.methodData.deprecated)
                .addTagsItem("resource:" + restClassData.className)
                .requestBody(getRequestBody(method.requestData, method.methodData, components))
                .responses(getApiResponses(restClassData, method.responseData, method.methodData, components));


        if (method.methodData.deprecated) {
            operation.addExtension("x-deprecated-description", expand(method.methodData.deprecatedDescription));
        }

        if (!method.methodData.links.isEmpty()) {
            List<Map<String, String>> linkedMethods = new ArrayList<>();

            for (ExecutableElement link : method.methodData.links) {
                for (RestClassData restClass : allRestClasses) {
                    Optional<RestMethodData> linkedMethod = restClass.methods.stream().filter(m -> m.methodData.methodDoc.equals(link)).findFirst();
                    linkedMethod.ifPresent(restMethodData -> linkedMethods.add(getLinkedMethodMap(restMethodData)));
                }
            }

            operation.addExtension("x-linked-methods", linkedMethods);
        }

        if (method.methodData.printCurl) {
            operation.addExtension("x-curl-enabled", true);
            if (method.methodData.curl != null) {
                operation.addExtension("x-curl-example", method.methodData.curl);
            }
        }

        List<RestUsecaseData.UsecaseData> relatedUsecases = getRelatedUsecases(restUsecases, method);

        if (!relatedUsecases.isEmpty()) {
            List<String> usecaseIds = relatedUsecases.stream().map(usecase -> usecase.id).toList();
            operation.addExtension("x-related-usecases", usecaseIds);
        }

        if (!method.methodData.allowedRoles.isEmpty()) {
            List<String> sortedRoles = new ArrayList<>(method.methodData.allowedRoles.stream().sorted().toList());
            operation.addExtension("x-allowed-roles", sortedRoles);
        }

        return operation;
    }

    private ApiResponses getApiResponses(RestClassData restClassData, List<RestMethodData.ResponseData> responseData,
            RestMethodData.MethodData methodData, Components components) {

        ApiResponses apiResponses = new ApiResponses();

        // add common responses
        restClassData.commonResponseData.forEach(response -> {
            ApiResponse apiResponse = getApiResponse(restClassData, methodData, components, response);

            apiResponses.addApiResponse(String.valueOf(response.status.value), apiResponse);
        });

        // add (and possibly overwrite) specific responses
        responseData.forEach(response -> {
            ApiResponse apiResponse = getApiResponse(restClassData, methodData, components, response);

            apiResponses.addApiResponse(String.valueOf(response.status.value), apiResponse);
        });

        return apiResponses;
    }

    private ApiResponse getApiResponse(RestClassData restClassData, RestMethodData.MethodData methodData, Components components,
            RestMethodData.ResponseData response) {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.description(expand(response.description));

        if (!response.entities.isEmpty()) {

            Content content = new Content();
            apiResponse.content(content);

            response.entities.forEach(entity -> {

                MediaType mediaType = new MediaType();
                if (StringUtils.isNotBlank(entity.description)) {
                    mediaType.addExtension("x-description", entity.description);
                }

                RestMethodData.ParameterInfo parameterInfo = new RestMethodData.ParameterInfo();
                parameterInfo.entityClass = entity.entityClass.equals(options.environment.getElementUtils()
                        .getTypeElement("java.lang.Void").asType())
                                            ? null : entity.entityClass;
                parameterInfo.nestedParameters = entity.nestedParameters;

                if (parameterInfo.entityClass != null) {
                    mediaType.schema(toSchema(parameterInfo, entity.entityClass, Collections.emptyMap(),
                            null, methodData, true, components, false));
                }

                if (entity.contentType == null) {
                    System.err.println("Null content type for " + entity);
                }

                content.addMediaType(entity.contentType, mediaType);

                entity.examples.forEach(example -> {
                    Example ex = new Example();
                    ex.value(example.value)
                            .description(expand(example.description));
                    mediaType.addExamples(example.title, ex);
                });

            });
        }

        response.headers.forEach(headerInfo -> {
            Header header = getHeader(headerInfo);

            RestMethodData.HeaderInfo headerDefinition = restClassData.headerDefinitions.get(headerInfo.name);

            if (headerDefinition != null && StringUtils.isBlank(header.getDescription())) {
                header.description(expand(headerDefinition.description));
            }

            apiResponse.addHeaderObject(headerInfo.name, header);
        });

        if (!response.cookies.isEmpty()) {
            apiResponse.addHeaderObject("Set-Cookie", getHeader(restClassData, response.cookies));
        }

        return apiResponse;
    }

    private Header getHeader(RestMethodData.HeaderInfo headerInfo) {
        Header header = new Header();

        header.description(expand(headerInfo.description));
        header.required(headerInfo.isRequired());
        header.deprecated(headerInfo.deprecated);

        if (headerInfo.deprecated) {
            header.addExtension("x-deprecated-description", expand(headerInfo.deprecatedDescription));
        }

        if (!headerInfo.allowedValues.isEmpty()) {
            List<Map<String, String>> allowedValueList = new ArrayList<>();
            headerInfo.allowedValues.forEach(allowedValue -> {
                HashMap<String, String> allowedValueMap = new HashMap<>();
                allowedValueMap.put("value", allowedValue.value);
                allowedValueMap.put("valueHint", allowedValue.valueHint);
                allowedValueList.add(allowedValueMap);
            });

            header.addExtension("x-allowed-values", allowedValueList);
        }

        return header;
    }

    private Header getHeader(RestClassData restClassData, List<RestMethodData.CookieInfo> cookies) {
        Header header = new Header();

        header.explode(true);

        Schema schema = new Schema();
        header.schema(schema);
        schema.type("array");

        List<Schema> items = new LinkedList<>();
        schema.allOf(items);

        cookies.forEach(cookieInfo -> {
            Schema item = new Schema();
            items.add(item);

            Map<String, String> options = new HashMap<>();

            RestMethodData.CookieInfo cookieDefinition = restClassData.cookieDefinitions.get(cookieInfo.name);

            if (cookieDefinition != null) {
                if (StringUtils.isNotBlank(cookieDefinition.description)) {
                    item.description(expand(cookieDefinition.description));
                }

                collectCookieOptions(options, cookieDefinition);
            }


            item.title(cookieInfo.name);
            if (StringUtils.isNotBlank(cookieInfo.description)) {
                item.description(expand(cookieInfo.description));
            }
            if (cookieInfo.isRequired()) {
                item.required(List.of("self"));
            }
            item.deprecated(cookieInfo.deprecated);

            collectCookieOptions(options, cookieInfo);

            item.addExtension("x-cookie-options", options);

            if (cookieInfo.deprecated) {
                item.addExtension("x-deprecated-description", expand(cookieInfo.deprecatedDescription));
            }

            if (!cookieInfo.allowedValues.isEmpty()) {
                List<Map<String, String>> allowedValueList = new ArrayList<>();
                cookieInfo.allowedValues.forEach(allowedValue -> {
                    HashMap<String, String> allowedValueMap = new HashMap<>();
                    allowedValueMap.put("value", allowedValue.value);
                    allowedValueMap.put("valueHint", allowedValue.valueHint);
                    allowedValueList.add(allowedValueMap);
                });

                item.addExtension("x-allowed-values", allowedValueList);
            }

        });


        return header;
    }

    private static void collectCookieOptions(Map<String, String> options, RestMethodData.CookieInfo cookieDefinition) {
        if (cookieDefinition.httpOnly != null) {
            options.put("httpOnly", String.valueOf(cookieDefinition.httpOnly));
        }
        if (cookieDefinition.secure != null) {
            options.put("secure", String.valueOf(cookieDefinition.secure));
        }
        if (cookieDefinition.sameSite != CookieSameSite.UNSET) {
            options.put("sameSite", cookieDefinition.sameSite.name());
        }
        if (StringUtils.isNotBlank(cookieDefinition.domain)) {
            options.put("domain", cookieDefinition.domain);
        }
        if (StringUtils.isNotBlank(cookieDefinition.path)) {
            options.put("path", cookieDefinition.path);
        }
        if (StringUtils.isNotBlank(cookieDefinition.maxAge)) {
            options.put("maxAge", cookieDefinition.maxAge);
        }
    }

    private List<RestUsecaseData.UsecaseData> getRelatedUsecases(List<RestUsecaseData> restUsecases, RestMethodData method) {
        return restUsecases.stream()
                .flatMap(u -> u.usecases.stream())
                .filter(usecase -> usecase.methods.stream()
                        .filter(usecaseMethodData -> usecaseMethodData.linkedMethod != null)
                        .anyMatch(usecaseMethodData -> usecaseMethodData.linkedMethod.equals(method)))
                .collect(Collectors.toList());
    }

    private List<Parameter> getParameterItems(RestClassData restClassData, List<RestMethodData.ParameterInfo> parameters,
            RestMethodData.MethodData methodData, Components components) {
        return parameters.stream()
                // body params are store in requestBody
                .filter(param -> param.parameterType != RestMethodData.ParameterType.BODY)
                .map(param -> {
                    Parameter parameter = new Parameter();

                    parameter.name(param.name)
                            .in(param.parameterType.name().toLowerCase())
                            .description(expand(param.description))
                            .deprecated(param.deprecated)
                            .required(param.isRequired())
                            .schema(toSchema(param, param.entityClass, new HashMap<>(), null, methodData, true,
                                    components, true));

                    parameter.addExtension("x-name-escaped", param.name.replaceAll("[^A-Za-z0-9]", "_"));

                    if (param.deprecated) {
                        parameter.addExtension("x-deprecated-description", expand(param.deprecatedDescription));
                    }

                    if (param.parameterType == RestMethodData.ParameterType.HEADER && StringUtils.isBlank(param.description)) {
                        // lookup description form header definition
                        RestMethodData.HeaderInfo headerDefinition = restClassData.headerDefinitions.get(param.name);

                        if (headerDefinition != null) {
                            parameter.description(expand(headerDefinition.description));
                        }
                    }

                    if (!param.allowedValues.isEmpty()) {
                        List<Map<String, String>> allowedValueList = new ArrayList<>();
                        param.allowedValues.forEach(allowedValue -> {
                            HashMap<String, String> allowedValueMap = new HashMap<>();
                            allowedValueMap.put("value", allowedValue.value);
                            allowedValueMap.put("valueHint", allowedValue.valueHint);
                            allowedValueList.add(allowedValueMap);
                        });

                        parameter.addExtension("x-allowed-values", allowedValueList);
                    }

                    return parameter;
                })
                .collect(Collectors.toList());
    }

    private RequestBody getRequestBody(RestMethodData.RequestData requestData, RestMethodData.MethodData methodData,
            Components components) {

        Optional<RestMethodData.Entity> fallbackBodyParam = requestData.entities.stream()
                .filter(param -> StringUtils.isBlank(param.contentType))
                .findFirst();

        Map<String, RestMethodData.Entity> mediaTypesWithEntity = getMediaTypesWithEntity(requestData, fallbackBodyParam);

        if (mediaTypesWithEntity.isEmpty()) {
            return null;
        }

        RequestBody requestBody = new RequestBody();
        Content content = new Content();
        requestBody.content(content);

        mediaTypesWithEntity.forEach((type, entity) -> {
            MediaType mediaType = new MediaType();

            if (StringUtils.isNotBlank(entity.description)) {
                mediaType.addExtension("x-description", entity.description);
            }

            RestMethodData.ParameterInfo parameterInfo = new RestMethodData.ParameterInfo();
            parameterInfo.entityClass = entity.entityClass;
            parameterInfo.nestedParameters = entity.nestedParameters;

            mediaType.schema(toSchema(parameterInfo, parameterInfo.entityClass, Collections.emptyMap(),
                    null, methodData, true, components, true));

            entity.examples.forEach(example -> {
                Example ex = new Example();
                ex.value(example.value)
                        .description(expand(example.description));
                mediaType.addExamples(example.title, ex);
            });

            content.addMediaType(type, mediaType);
        });

        return requestBody;
    }

    private Map<String, RestMethodData.Entity> getMediaTypesWithEntity(RestMethodData.RequestData requestData,
            Optional<RestMethodData.Entity> fallbackBodyParam) {

        if (requestData.mediaType == null || requestData.mediaType.isEmpty()) {
            return Map.of();
        }

        return requestData.mediaType
                .stream().map(mediaType -> {
                    Optional<RestMethodData.Entity> entityForMediaType = requestData.entities.stream()
                            .filter(param -> mediaType.equals(param.contentType))
                            .findFirst();

                    if (entityForMediaType.isEmpty()) {
                        entityForMediaType = fallbackBodyParam;
                    }
                    return new AbstractMap.SimpleEntry<>(mediaType, entityForMediaType);
                })
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    private Schema toSchema(RestMethodData.ParameterInfo param, TypeMirror type, Map<String, String> parameterUsecaseValues,
            String parent, RestMethodData.MethodData methodData, boolean skipEnhance, Components components, boolean isRequest) {

        if (isCollectionType(param.entityClass)) {
            return processArrayType(param, param.entityClass, parameterUsecaseValues, parent, methodData, skipEnhance,
                    components, isRequest);
        } else if (isMapType(param.entityClass)) {
            return processMapType(param, param.entityClass, parameterUsecaseValues, parent, methodData, skipEnhance,
                    components, isRequest);
        } else {
            Schema schema = new ObjectSchema();
            schema.description(expand(param.description));
            schema.type(getOpenApiTypeString(param.entityClass, options.environment));
            schema.addExtension("x-java-type", getSimpleTypeName(param.entityClass, options.environment));
            schema.addExtension("x-java-type-required", param.isRequired());
            addConstraints(schema, param);
            addAllowedValues(schema, param);
            schema.deprecated(param.deprecated);
            if (!StringUtils.isBlank(param.deprecatedDescription)) {
                schema.addExtension("x-deprecated-description", expand(param.deprecatedDescription));
            }

            for (RestMethodData.ParameterInfo nestedParam : param.nestedParameters) {

                TypeMirror nestedEntityClass = nestedParam.displayClass != null
                                               ? nestedParam.displayClass
                                               : nestedParam.entityClass;

                if (isCollectionType(nestedEntityClass)) {
                    schema.addProperty(nestedParam.name, processArrayType(nestedParam, nestedEntityClass,
                            parameterUsecaseValues, parent, methodData, skipEnhance, components, isRequest));
                } else if (isMapType(nestedEntityClass)) {
                    schema.addProperty(nestedParam.name, processMapType(nestedParam, nestedEntityClass,
                            parameterUsecaseValues, parent, methodData, skipEnhance, components, isRequest));
                } else if (containedFieldNamesAreNotAvailableOrPackageExcluded(nestedEntityClass, options) // stop at 'arg0' etc. this does not provide useful information
                        || nestedEntityClass.equals(type)) {  // break simple recursive loops
                    schema.addProperty(nestedParam.name, getSimpleSchema(nestedParam, nestedEntityClass));
                } else {
                    schema.addProperty(nestedParam.name, processType(nestedParam, nestedEntityClass, nestedParam.name,
                            parameterUsecaseValues, concat(parent), methodData, skipEnhance, components, isRequest));
                }
            }

            schema.addExtension("x-java-type-expandable", schema.getProperties() != null);

            return cacheSchema(param, isRequest, schema, components);
        }
    }

    private Schema cacheSchema(RestMethodData.ParameterInfo param, boolean isRequest, Schema schema, Components components) {
        // schema must be fully generated every time since there could be minor differences, which must prevent reuse

        // look for an actually equal entry
        var existingSchemaKey = Optional.ofNullable(components.getSchemas())
                .flatMap(schemas -> schemas.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(schema))
                        .map(Map.Entry::getKey)
                        .findFirst()
                );

        if (existingSchemaKey.isPresent()) {
            Schema refSchema = new Schema();
            refSchema.$ref("#/components/schemas/" + existingSchemaKey.get());
            return refSchema;
        }

        // else create a new entry, if feasible
        if (!containedFieldNamesAreNotAvailableOrPackageExcluded(param.entityClass, options)) {

            var qualifiedName = (isRequest ? "request-" : "response-") + getQualifiedName(param.entityClass, options.environment);
            var key = qualifiedName;
            var counter = 1;

            while (components.getSchemas() != null && components.getSchemas().containsKey(key)) {
                key = qualifiedName + "_" + counter++;
            }

            components.addSchemas(key, schema);
            Schema refSchema = new Schema();
            refSchema.$ref("#/components/schemas/" + key);
            return refSchema;
        }

        return schema;
    }

    private Schema processType(RestMethodData.ParameterInfo param, TypeMirror type, String fieldName, Map<String, String> parameterUsecaseValues, String parent,
            RestMethodData.MethodData methodData, boolean skipEnhance, Components components, boolean isRequest) {

        Schema schema = new ObjectSchema();
        schema.type(getOpenApiTypeString(type, options.environment));
        schema.addExtension("x-java-type", getSimpleTypeName(type, options.environment));

        if (param != null) {
            schema.addExtension("x-java-type-required", param.isRequired());
            schema.description(expand(param.description));
            schema.deprecated(param.deprecated);
            if (!StringUtils.isBlank(param.deprecatedDescription)) {
                schema.addExtension("x-deprecated-description", expand(param.deprecatedDescription));
            }
        }
        addConstraints(schema, param);
        addAllowedValues(schema, param);

        if (isDocumentableSimpleType(type, fieldName)) {
            schema = toSchema(param, type, parameterUsecaseValues, concat(parent, fieldName),
                    methodData, skipEnhance, components, isRequest);
        } else if (isMapType(type)) {
            MapSchema mapSchema = processMapType(param, type, parameterUsecaseValues, concat(parent, fieldName),
                    methodData, skipEnhance, components, isRequest);

            if (fieldName != null) {
                schema.addProperty(fieldName, mapSchema);
            } else {
                schema = mapSchema;
            }
        } else if (isCollectionType(type)) {
            ArraySchema arraySchema = processArrayType(param, type, parameterUsecaseValues, concat(parent, fieldName),
                    methodData, skipEnhance, components, isRequest);

            if (fieldName != null) {
                schema.addProperty(fieldName, arraySchema);
            } else {
                throw new IllegalArgumentException("field name is required for array type");
            }

        } else if (fieldName != null) {
            schema = getSimpleSchema(param, type);
        }

        schema.addExtension("x-java-type-expandable", schema.getProperties() != null || schema.getAdditionalProperties() != null);

        return schema;
    }

    private MapSchema processMapType(RestMethodData.ParameterInfo param, TypeMirror type, Map<String, String> parameterUsecaseValues, String parent,
            RestMethodData.MethodData methodData, boolean skipEnhance, Components components, boolean isRequest) {
        MapSchema mapSchema = new MapSchema();
        mapSchema.addExtension("x-java-type", getSimpleTypeName(type, options.environment));
        mapSchema.addExtension("x-java-type-expandable", true);
        if (param != null) {
            mapSchema.addExtension("x-java-type-required", param.isRequired());
            mapSchema.description(expand(param.description));
            mapSchema.deprecated(param.deprecated);
            if (!StringUtils.isBlank(param.deprecatedDescription)) {
                mapSchema.addExtension("x-deprecated-description", expand(param.deprecatedDescription));
            }
        }
        addConstraints(mapSchema, param);
        addAllowedValues(mapSchema, param);

        RestMethodData.ParameterInfo keyType = param != null && !param.nestedParameters.isEmpty()
                                               ? param.nestedParameters.stream().filter(p -> p.name.equals("[key]")).findFirst().get()
                                               : null;

        if (keyType != null) {
            addAllowedMapKeyValues(mapSchema, keyType);
        }

        RestMethodData.ParameterInfo valueType = param != null && !param.nestedParameters.isEmpty()
                                                 ? param.nestedParameters.stream().filter(p -> p.name.equals("[value]")).findFirst().get()
                                                 : null;

        TypeMirror valueTypeMirror = valueType != null
                                     ? valueType.entityClass
                                     : ((DeclaredType) type).getTypeArguments().get(1);

        if (isCollectionType(valueTypeMirror)) {
            mapSchema.additionalProperties(processArrayType(valueType, valueTypeMirror, parameterUsecaseValues, parent,
                    methodData, skipEnhance, components, isRequest));
        } else if (valueTypeMirror != null && asElement(valueTypeMirror, options.environment) != null && !valueTypeMirror.getKind().isPrimitive()
                && !getQualifiedName(valueTypeMirror, options.environment).startsWith("java.lang") && !isEnum(valueTypeMirror, options.environment)) {

            mapSchema.additionalProperties(processType(valueType, valueTypeMirror, null, parameterUsecaseValues, parent,
                    methodData, skipEnhance, components, isRequest));
        } else {
            Schema schema = new Schema();
            schema.addExtension("x-java-type", getSimpleTypeName(valueTypeMirror, options.environment));
            schema.addExtension("x-java-type-expandable", !typeCantBeDocumented(valueTypeMirror, options));

            schema.type(getOpenApiTypeString(valueTypeMirror, options.environment));

            if (valueType != null) {
                addConstraints(schema, valueType);
                addAllowedValues(schema, valueType);
            }

            mapSchema.additionalProperties(schema);
        }

        return mapSchema;
    }

    private ArraySchema processArrayType(RestMethodData.ParameterInfo param, TypeMirror type,
            Map<String, String> parameterUsecaseValues, String parent,
            RestMethodData.MethodData methodData, boolean skipEnhance, Components components, boolean isRequest) {

        ArraySchema arraySchema = new ArraySchema();
        arraySchema.addExtension("x-java-type", getSimpleTypeName(type, options.environment));

        if (param != null) {
            arraySchema.addExtension("x-java-type-required", param.isRequired());
            arraySchema.description(expand(param.description));
            arraySchema.deprecated(param.deprecated);
            if (!StringUtils.isBlank(param.deprecatedDescription)) {
                arraySchema.addExtension("x-deprecated-description", expand(param.deprecatedDescription));
            }
        }
        addConstraints(arraySchema, param);
        addAllowedValues(arraySchema, param);

        RestMethodData.ParameterInfo valueType = param != null && !param.nestedParameters.isEmpty()
                                                 ? param.nestedParameters.get(0)
                                                 : null;

        TypeMirror fallbackValue = type instanceof ArrayType
                                   ? ((ArrayType) type).getComponentType()
                                   : ((DeclaredType) type).getTypeArguments().get(0);

        TypeMirror valueTypeMirror = valueType != null
                                     ? valueType.entityClass
                                     : fallbackValue;

        if (isCollectionType(valueTypeMirror)) {
            arraySchema.items(processArrayType(valueType, valueTypeMirror, parameterUsecaseValues, parent, methodData,
                    skipEnhance, components, isRequest));
        } else if (isMapType(valueTypeMirror)) {
            arraySchema.items(processMapType(valueType, valueTypeMirror, parameterUsecaseValues, parent, methodData,
                    skipEnhance, components, isRequest));
        } else if (valueType != null && asElement(valueTypeMirror, options.environment) != null && !valueTypeMirror.getKind().isPrimitive()
                && !getQualifiedName(valueTypeMirror, options.environment).startsWith("java.lang") && !isEnum(valueTypeMirror, options.environment)) {
            arraySchema.items(toSchema(valueType, valueTypeMirror, parameterUsecaseValues, parent, methodData,
                    skipEnhance, components, isRequest));
        } else {
            arraySchema.items(getSimpleSchema(valueType, valueTypeMirror));
        }

        arraySchema.addExtension("x-java-type-expandable",
                arraySchema.getItems() != null && valueTypeMirror.getKind() != TypeKind.BYTE);

        return arraySchema;
    }

    private Schema getSimpleSchema(RestMethodData.ParameterInfo param, TypeMirror type) {
        Schema schema = new Schema();
        schema.addExtension("x-java-type", getSimpleTypeName(type, options.environment));
        schema.addExtension("x-java-type-expandable", false);
        schema.type(getOpenApiTypeString(type, options.environment));
        if (param != null) {
            schema.addExtension("x-java-type-required", param.isRequired());
            schema.description(expand(param.description));
            schema.deprecated(param.deprecated);
            if (!StringUtils.isBlank(param.deprecatedDescription)) {
                schema.addExtension("x-deprecated-description", expand(param.deprecatedDescription));
            }
        }
        addConstraints(schema, param);
        addAllowedValues(schema, param);

        if (isEnum(type, options.environment)) {
            schema.setEnum(getEnumValuesAsList(type, options.environment).stream().map(ev -> ev.getSimpleName().toString())
                    .collect(Collectors.toList()));
        }


        return schema;
    }

    private void addAllowedValues(Schema schema, RestMethodData.ParameterInfo param) {
        addAllowedValues(schema, param, "x-allowed-values");
    }

    private void addAllowedMapKeyValues(Schema schema, RestMethodData.ParameterInfo param) {
        addAllowedValues(schema, param, "x-allowed-key-values");
    }

    private void addAllowedValues(Schema schema, RestMethodData.ParameterInfo param, String extensionName) {
        if (param == null) {
            return;
        }

        if (!param.allowedValues.isEmpty()) {
            List<Map<String, String>> allowedValueList = new ArrayList<>();
            param.allowedValues.forEach(allowedValue -> {
                HashMap<String, String> allowedValueMap = new HashMap<>();
                allowedValueMap.put("value", allowedValue.value);
                allowedValueMap.put("valueHint", allowedValue.valueHint);
                allowedValueList.add(allowedValueMap);
            });

            schema.addExtension(extensionName, allowedValueList);
        }
    }

    private void addConstraints(Schema schema, RestMethodData.ParameterInfo param) {
        if (param == null || param.constraints == null || param.constraints.isEmpty()) {
            return;
        }

        param.constraints.forEach((name, constraint) -> {
            switch (name) {
                case "Pattern":
                    Optional.ofNullable(constraint.get("regexp")).ifPresent(schema::pattern);
                    break;
                case "Size":
                    if (isMapType(param.entityClass) || isCollectionType(param.entityClass)) {
                        getInteger(constraint, "min").ifPresent(schema::minItems);
                        getInteger(constraint, "max").ifPresent(schema::maxItems);
                    } else {
                        getInteger(constraint, "min").ifPresent(schema::minLength);
                        getInteger(constraint, "max").ifPresent(schema::maxLength);
                    }
                    break;
                case "Min":
                    getBigDecimal(constraint, "value").ifPresent(schema::minimum);
                    break;
                case "Max":
                    getBigDecimal(constraint, "value").ifPresent(schema::maximum);
                    break;
                case "Positive":
                    schema.minimum(BigDecimal.ONE);
                    break;
                case "PositiveOrZero":
                    schema.minimum(BigDecimal.ZERO);
                    break;
                case "Negative":
                    schema.maximum(new BigDecimal(-1));
                    break;
                case "NegativeOrZero":
                    schema.maximum(BigDecimal.ZERO);
                    break;
                default:
            }
        });
    }

    private Optional<BigDecimal> getBigDecimal(Map<String, String> map, String key) {
        return getInteger(map, key).map(BigDecimal::valueOf);
    }

    private Optional<Integer> getInteger(Map<String, String> map, String key) {
        return Optional.ofNullable(map.get(key)).map(Integer::parseInt);
    }

}
