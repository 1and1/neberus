package net.oneandone.neberus.test.springwebmvc;

import net.oneandone.neberus.annotation.ApiAllowedValue;
import net.oneandone.neberus.annotation.ApiCommonResponse;
import net.oneandone.neberus.annotation.ApiCurl;
import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiDocumentation;
import net.oneandone.neberus.annotation.ApiEntity;
import net.oneandone.neberus.annotation.ApiExample;
import net.oneandone.neberus.annotation.ApiHeader;
import net.oneandone.neberus.annotation.ApiHeaderDefinition;
import net.oneandone.neberus.annotation.ApiIgnore;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.annotation.ApiOptional;
import net.oneandone.neberus.annotation.ApiParameter;
import net.oneandone.neberus.annotation.ApiRequestEntity;
import net.oneandone.neberus.annotation.ApiRequired;
import net.oneandone.neberus.annotation.ApiResponse;
import net.oneandone.neberus.annotation.ApiType;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.test.request.SomeChildFieldDto;
import net.oneandone.neberus.test.request.SomeCtorDto;
import net.oneandone.neberus.test.request.SomeFieldDto;
import net.oneandone.neberus.test.request.SomeGetterDto;
import net.oneandone.neberus.test.response.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Max;

/**
 * REST Class Documentation
 */
@ApiDocumentation
@RequestMapping(path = "/rootPath", name = "Super Awesome REST Service")
@ApiHeaderDefinition(name = "header1", description = "description1")
@ApiHeaderDefinition(name = "header2", description = "description2")
@ApiHeaderDefinition(name = "Predefined", description = "one description to rule them all")
@ApiCommonResponse(status = ApiStatus.INTERNAL_SERVER_ERROR, description = "internal server error defined on class")
public class RestService {

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     *
     * @deprecated use this one {@link #justYetAnotherGetMethod(String, String, String, String, SomeFieldDto, SomeCtorDto, SomeChildFieldDto)}
     * or that one {@link #justYetAnotherGetMethod(String, String, String, String, SomeFieldDto, SomeCtorDto, SomeChildFieldDto)}
     * or even the one from the other resource {@link RestServiceWithInterfaceDoc#getMethod(String, String, String)}
     */
    @RequestMapping(method = RequestMethod.GET,
                    path = "/anotherGet/{pathParam}/anotherPathParam/{anotherPathParam}/{wrappedPathParam}",
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiLabel("This is an awesome method")
    @ApiDescription("Description of this awesomely awesome method defined in annotation!")
    @ApiResponse(status = ApiStatus.OK, description = "success",
                 headers = {
                         @ApiHeader(name = "header2", allowedValues = {
                                 @ApiAllowedValue(value = "this one value", valueHint = "only that one"),
                                 @ApiAllowedValue(value = "this other value", valueHint = "or only that one")
                         }) },
                 entities = {
                         @ApiEntity(entityClass = SomeFieldDto.class, examples = {
                                 @ApiExample(title = "example response", value = "{\"jsonParam\":\"some value example\"}"),
                                 @ApiExample(title = "other example response", value = "{\"jsonParam\":\"some other value example\"}")
                         })
                 })
    @ApiResponse(status = ApiStatus.BAD_REQUEST, description = "success", headers = {
            @ApiHeader(name = "header2", allowedValues = {
                    @ApiAllowedValue(value = "this second value", valueHint = "only that one"),
                    @ApiAllowedValue(value = "this second other value", valueHint = "or only that one")
            })
    })
    @ApiParameter(name = "anotherQueryParam", type = ApiParameter.Type.QUERY, deprecated = true, deprecatedDescription = "use queryParam instead",
                  optional = true)
    @ApiCurl
    @Deprecated
    public String justAnotherGetMethod(@PathVariable @ApiAllowedValue("default") String pathParam,
                                       @PathVariable("anotherPathParam") String anotherPathParam,
                                       @PathVariable("wrappedPathParam") @ApiType(String.class) WrappedString wrappedPathParam,
                                       @Deprecated @RequestParam("queryParam") String queryParam) {
        return "";
    }

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     *
     * @param header     header javadoc <a href='index.html'>here</a>
     * @param pathParam  pathdoc
     * @param queryParam {@link SomeEnum}
     */
    @PatchMapping(value = "/anotherPatch",
                  consumes = { MediaType.APPLICATION_JSON_VALUE,
                          MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                          MediaType.APPLICATION_XML_VALUE })
    @ApiLabel("This is another awesome method")
    @ApiCurl
    @ApiResponse(status = ApiStatus.BAD_REQUEST, description = "Client Error", entities = {
            @ApiEntity(entityClass = Problem.class, contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    })
    @ApiResponse(status = ApiStatus.OK, description = "success", entities = {
            @ApiEntity(entityClass = Problem.class, contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE),
            @ApiEntity(entityClass = SomeFieldDto.class, contentType = MediaType.APPLICATION_JSON_VALUE)
    })
    @ApiParameter(name = "headerParam", description = "custom description <a href='index.html'>here</a>", type = ApiParameter.Type.HEADER)
    public void justYetAnotherGetMethod(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String header,
                                        @ApiRequired @RequestHeader(value = "requiredOptionalHeader", required = false) String requiredOptionalHeader,
                                        @PathVariable("pathParam") @ApiAllowedValue("the expected default value") String pathParam,
                                        @RequestParam(value = "queryParam", required = false) String queryParam,
                                        @ApiIgnore @RequestParam(value = "queryParamIgnored") String queryParamIgnored,
                                        @RequestBody SomeFieldDto dto, @RequestBody SomeCtorDto otherDto, SomeChildFieldDto childDto) {

    }

    @RequestMapping(method = { RequestMethod.PATCH, RequestMethod.PUT },
                    path = "/againAnotherGet",
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    name = "This is another awesome method again")
    @ApiResponse(status = ApiStatus.OK, description = "success")
    @ApiCurl
    public SomeFieldDto againAnotherGetMethod(@RequestBody SomeGetterDto dto) {
        return null;
    }


    @DeleteMapping(path = "/delete",
                   name = "The first awesome delete method")
    @ApiResponse(status = ApiStatus.OK, description = "success")
    @ApiCurl
    public void deleteMethod() {
    }

    @GetMapping(path = "/getEntity",
                name = "The first awesome get entity method",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(status = ApiStatus.OK, description = "success", entities = {
            @ApiEntity(entityClass = SomeFieldDto.class)
    })
    @ApiCurl
    @ApiParameter(name = "Authorization", type = ApiParameter.Type.HEADER, description = "the authorization header", optional = true)
    @ApiParameter(name = "header1", type = ApiParameter.Type.HEADER)
    public ResponseEntity<?> getEntityMethod(@RequestHeader HttpHeaders ignored) {
        return null;
    }

    @PostMapping(path = "/post/form/generic",
                 consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiCurl
    @ApiRequestEntity(entityClass = GenericForm.class)
    public ResponseEntity<?> putGenericFormMethod(@RequestBody final MultiValueMap<String, String> parameters) {
        return null;
    }

    @GetMapping(name = "Get without explicit path",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(status = ApiStatus.OK, description = "success", entities = {
            @ApiEntity(entityClass = SomeFieldDto.class)
    })
    @ApiCurl
    public ResponseEntity<?> getRootMethod() {
        return null;
    }

    public static class WrappedString {

        public String value;
    }

    public static class GenericForm {
        /**
         * formString javadoc
         */
        @ApiAllowedValue("hallo")
        public String formString;

        @Max(5)
        @ApiOptional
        public int formInt;
    }

}
