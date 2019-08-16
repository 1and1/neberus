package net.oneandone.neberus.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.oneandone.neberus.annotation.*;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.model.ProblemType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * REST Class Documentation
 */
@ApiDocumentation
@Path("/rootPath")
@ApiLabel("Super Relefant REST Service")
@ApiHeader(name = "header1", description = "description1")
@ApiHeader(name = "header2", description = "description2")
public class RestService1 {

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     *
     * @param dto the body
     */
    @POST
    @Path("/anotherGet/{pathParam}/anotherPathParam/{anotherPathParam}")
    @ApiLabel("This is an awesome method")
    @Consumes(MediaType.APPLICATION_JSON)
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
            @ApiHeader(name = "header2") }
    )
    @ApiCurl
    public void justAnotherGetMethod(@PathParam("pathParam") @ApiAllowedValues("default") String pathParam,
                                     @PathParam("anotherPathParam") String anotherPathParam,
                                     @QueryParam("queryParam") String queryParam,
                                     ExternalDto dto) {

    }

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     */
    @PUT
    @Path("/anotherGet")
    @ApiLabel("This is another awesome method")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiCurl
    @ApiSuccessResponse(status = ApiStatus.BAD_REQUEST)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = FailureResultDto.class, contentType = MediaType.APPLICATION_JSON)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = SomeDto.class, contentType = MediaType.APPLICATION_JSON)
    @ApiParameter(name = "jsonParam", description = "custom description", containerClass = SomeDto.class)
    @ApiResponseValue(name = "jsonParam2", description = "custom description", containerClass = SomeDto.class)
    @ApiResponseValue(name = "custom responseValue2", description = "custom description")
    public void justYetAnotherGetMethod(@PathParam("pathParam") @ApiAllowedValues("the expected default value") String pathParam,
                                        @QueryParam("queryParam") String queryParam,
                                        ExternalDto dto) {

    }

    public static class SomeDto {

        public String jsonParam;
        public String jsonParam2;
        public int jsonIntParam;
        public byte[] jsonbyteArrayParam;

        @JsonIgnore
        public String ignoreThisParam;
    }

}
