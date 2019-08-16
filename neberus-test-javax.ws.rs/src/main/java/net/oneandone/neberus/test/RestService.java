package net.oneandone.neberus.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.included.IncludedDto;
import com.notincluded.NotIncludedDto;
import net.oneandone.neberus.ResponseType;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.model.ProblemType;
import net.oneandone.neberus.annotation.*;
import net.oneandone.neberus.parse.RestMethodData;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST Class Documentation
 */
@ApiDocumentation
@Path("/rootPath")
@ApiLabel("Super Awesome REST Service")
@ApiHeader(name = "header1", description = "description1")
@ApiHeader(name = "header2", description = "description2")
@ApiHeader(name = "Predefined", description = "one description to rule them all")
public class RestService {

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     *
     * @deprecated use this one {@link #justYetAnotherGetMethod(String, String, String, SomeDto, SomeCtorDto, SomeChildDto)}
     * or that one {@link #justYetAnotherGetMethod(String, String, String, SomeDto, SomeCtorDto, SomeChildDto)}
     * or even the one from the other resource
     * {@link RestService1#justAnotherGetMethod(String, String, String, ExternalDto)}
     */
    @GET
    @Path("/anotherGet/{pathParam}/anotherPathParam/{anotherPathParam}/{wrappedPathParam}")
    @ApiLabel("This is an awesome method")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiDescription("Description of this awesomely awesome method defined in annotation!")
    @ApiSuccessResponse(status = ApiStatus.OK)
    @ApiProblemResponse(status = ApiStatus.OK, description = "this should be handled as error",
                        type = ProblemType.EXPECTATION_FAILED, detail = "magic failed", title = "magic title")
    @ApiWarningResponse(status = ApiStatus.OK, description = "and this as warning", warnings =
    @ApiWarning(type = ProblemType.AUTHENTICATION_ERROR, title = "warning title"))
    @ApiSuccessResponse(status = ApiStatus.BAD_GATEWAY, description = "a bad thing happened", entityClass = SomeDto.class,
                        contentType = "crazyCustomType", headers = {
            @ApiHeader(name = "123", description = "456"),
            @ApiHeader(name = "header2") })
    @ApiCurl
    @Deprecated
    public void justAnotherGetMethod(@PathParam("pathParam") @ApiAllowedValues("default") String pathParam,
                                     @PathParam("anotherPathParam") String anotherPathParam,
                                     @PathParam("wrappedPathParam") @ApiType(String.class) WrappedString wrappedPathParam,
                                     @QueryParam("queryParam") String queryParam) {

    }

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     *
     * @param header     header javadoc <a href='index.html'>here</a>
     * @param pathParam  pathdoc
     * @param queryParam {@link SomeEnum}
     */
    @PATCH
    @Path("/anotherGet")
    @ApiLabel("This is another awesome method")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiCurl
    @ApiResponse(status = ApiStatus.BAD_REQUEST, responseType = ResponseType.GENERIC)
    @ApiResponse(status = ApiStatus.BAD_REQUEST, responseType = ResponseType.PROBLEM)
    @ApiResponse(status = ApiStatus.BAD_REQUEST, responseType = ResponseType.WARNING, contentType = MediaType.APPLICATION_JSON,
                 entityClass = FailureResultDto.class)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = FailureResultDto.class, contentType = MediaType.APPLICATION_JSON)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = SomeDto.class, contentType = MediaType.APPLICATION_JSON)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = SomeChildDto.class, contentType = MediaType.APPLICATION_JSON)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = SomeChildDto.class, contentType = MediaType.APPLICATION_XML)
    @ApiParameter(name = "jsonParam", description = "custom description", containerClass = SomeDto.class)
    @ApiParameter(name = "jsonParam", description = "custom description", containerClass = SomeCtorDto.class, optional = true)
    @ApiParameter(name = "headerParam", description = "custom description <a href='index.html'>here</a>", type = RestMethodData.ParameterType.HEADER)
    @ApiResponseValue(name = "jsonParam2", description = "custom description", containerClass = SomeCtorDto.class)
    @ApiResponseValue(name = "custom responseValue2", description = "custom description")
    public void justYetAnotherGetMethod(@HeaderParam(HttpHeaders.AUTHORIZATION) @Size(max = 42) String header,
                                        @PathParam("pathParam") @ApiAllowedValues(
                                                value = { "the", "expected", "allowed", "values" },
                                                valueHint = "something like this") @Size(max = 42) String pathParam,
                                        @ApiOptional @QueryParam("queryParam") @Size(max = 42) String queryParam,
                                        SomeDto dto, SomeCtorDto otherDto, SomeChildDto childDto) {

    }

    @PATCH
    @Path("{pathParamWithoutSlash}")
    @ApiLabel("This is another awesome method again")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiCurl
    public void againAnotherGetMethod(SomeGetterDto dto, @PathParam("pathParamWithoutSlash") String pathparam) {

    }

    @POST
    @Path("/postFormParams")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiCurl
    @ApiResponse(status = ApiStatus.BAD_REQUEST, responseType = ResponseType.PROBLEM)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = SomeDto.class, contentType = MediaType.APPLICATION_JSON)
    public Response postMethodWithForParams(@FormParam("param1") String formParam1,
                                            @FormParam("param2") @ApiType(String.class) WrappedString formParam2,
                                            @FormParam("param3") int formParam3) {
        return null;
    }

    public static class WrappedString {

        public String value;
    }

    public static class SomeDto {

        /**
         * my fieldDoc
         *
         * @see SomeEnum
         */
        public String jsonParam;
        /**
         * my fieldDoc {@link SomeEnum}
         */
        @JsonProperty("I_AM_FIELDMAN")
        @Pattern(regexp = "abc.123")
        public String jsonParam2;

        /**
         * @see IncludedDto#constraintReference
         */
        public String referencedConstraintsParam;

        @ApiOptional
        @ApiAllowedValues(value = "33", valueHint = "[-5, 42]")
        @Min(-5)
        @Max(42)
        public int jsonIntParam;

        @ApiAllowedValues(enumValues = SomeEnum.class)
        public byte[] jsonbyteArrayParam;

        @JsonIgnore
        public String ignoreThisParam;

        @Size(max = 42)
        public Map<String, String> simpleMap;

        public Map<String, Map<String, String>> nestedMap;

        public List<String> simpleList;
        public List<List<String>> nestedList;

        public Set<String> simpleSet;
        public Set<Set<String>> nestedSet;

        public Map<String, List<Set<String>>> nestedMagic;
        public List<Map<String, Set<String>>> nestedMagic2;
        public SomeCtorDto someCtorDto;
        public SomeEnum someEnum;
        public List<NestedDto> nestedDtoList;

        public IncludedDto includedDto;
        public NotIncludedDto notIncludedDto;

    }

    public enum SomeEnum {
        FIRST, SECOND, THIRD
    }

    public static class NestedDto {

        public String nestedString;
    }

    public static class SomeChildDto extends SomeDto {

        public String childValue;
    }

    public static class SomeCtorDto {

        private final String jsonParam;
        private final String jsonParam2;
        private final int jsonIntParam;
        private final byte[] jsonbyteArrayParam;

        private final String ignoreThisParam;

        private final Map<String, String> simpleMap;
        private final Map<String, Map<String, String>> nestedMap;

        private final List<String> simpleList;
        private final List<List<String>> nestedList;

        private final Set<String> simpleSet;
        private final Set<Set<String>> nestedSet;

        private final Map<String, List<Set<String>>> nestedMagic;
        private final List<Map<String, Set<String>>> nestedMagic2;
        private final List<NestedDto> nestedDtoList;
        private final NestedDto nestedDto;

        /**
         * @param jsonParam          paramDoc {@link SomeEnum}
         * @param jsonParam2         paramDoc
         * @param jsonIntParam
         * @param jsonbyteArrayParam
         * @param ignoreThisParam
         * @param simpleMap
         * @param nestedMap
         * @param simpleList
         * @param nestedList
         * @param simpleSet
         * @param nestedSet
         * @param nestedMagic
         * @param nestedMagic2
         * @param nestedDto
         * @param nestedDtoList
         */
        public SomeCtorDto(String jsonParam, @JsonProperty("I_AM_CTORMAN") @Pattern(regexp = "abc.123") String jsonParam2,
                           @ApiOptional @ApiAllowedValues(value = "33", valueHint = "[-5, 42]") @Min(-5) @Max(42) int jsonIntParam,
                           byte[] jsonbyteArrayParam, String ignoreThisParam, @Size(max = 42) Map<String, String> simpleMap,
                           Map<String, Map<String, String>> nestedMap, List<String> simpleList, List<List<String>> nestedList,
                           Set<String> simpleSet, Set<Set<String>> nestedSet, Map<String, List<Set<String>>> nestedMagic,
                           List<Map<String, Set<String>>> nestedMagic2, NestedDto nestedDto, List<NestedDto> nestedDtoList) {
            this.jsonParam = jsonParam;
            this.jsonParam2 = jsonParam2;
            this.jsonIntParam = jsonIntParam;
            this.jsonbyteArrayParam = jsonbyteArrayParam;
            this.ignoreThisParam = ignoreThisParam;
            this.simpleMap = simpleMap;
            this.nestedMap = nestedMap;
            this.simpleList = simpleList;
            this.nestedList = nestedList;
            this.simpleSet = simpleSet;
            this.nestedSet = nestedSet;
            this.nestedMagic = nestedMagic;
            this.nestedMagic2 = nestedMagic2;
            this.nestedDto = nestedDto;
            this.nestedDtoList = nestedDtoList;
        }

    }

    public static class SomeGetterDto {

        private String jsonParam;
        private String jsonParam2;
        private int jsonIntParam;
        private byte[] jsonbyteArrayParam;

        private String ignoreThisParam;

        private Map<String, String> simpleMap;
        private Map<String, Map<String, String>> nestedMap;

        private List<String> simpleList;
        private List<List<String>> nestedList;

        private Set<String> simpleSet;
        private Set<Set<String>> nestedSet;

        private Map<String, List<Set<String>>> nestedMagic;
        private List<Map<String, Set<String>>> nestedMagic2;
        private List<NestedDto> nestedDtoList;
        private NestedDto nestedDto;

        /**
         * this is the comment text
         */
        public String getJsonParam() {
            return jsonParam;
        }

        /**
         * this is shadowed by the return tag
         *
         * @return this is the return tag text
         */
        @JsonProperty("I_AM_GETTERMAN")
        @Pattern(regexp = "abc.123")
        public String getJsonParam2() {
            return jsonParam2;
        }

        @ApiOptional
        @ApiAllowedValues(value = "33", valueHint = "[-5, 42]")
        @Min(-5)
        @Max(42)
        public int getJsonIntParam() {
            return jsonIntParam;
        }

        public byte[] getJsonbyteArrayParam() {
            return jsonbyteArrayParam;
        }

        public String getIgnoreThisParam() {
            return ignoreThisParam;
        }

        @Size(max = 42)
        public Map<String, String> getSimpleMap() {
            return simpleMap;
        }

        public Map<String, Map<String, String>> getNestedMap() {
            return nestedMap;
        }

        public List<String> getSimpleList() {
            return simpleList;
        }

        public List<List<String>> getNestedList() {
            return nestedList;
        }

        public Set<String> getSimpleSet() {
            return simpleSet;
        }

        public Set<Set<String>> getNestedSet() {
            return nestedSet;
        }

        public Map<String, List<Set<String>>> getNestedMagic() {
            return nestedMagic;
        }

        public List<Map<String, Set<String>>> getNestedMagic2() {
            return nestedMagic2;
        }

        public List<NestedDto> getNestedDtoList() {
            return nestedDtoList;
        }

        public NestedDto getNestedDto() {
            return nestedDto;
        }

    }

}
