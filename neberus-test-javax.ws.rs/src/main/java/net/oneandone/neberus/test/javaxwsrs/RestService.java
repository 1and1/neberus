package net.oneandone.neberus.test.javaxwsrs;

import net.oneandone.neberus.annotation.ApiAllowedValue;
import net.oneandone.neberus.annotation.ApiCurl;
import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiDocumentation;
import net.oneandone.neberus.annotation.ApiEntity;
import net.oneandone.neberus.annotation.ApiHeaderDefinition;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.annotation.ApiOptional;
import net.oneandone.neberus.annotation.ApiParameter;
import net.oneandone.neberus.annotation.ApiResponse;
import net.oneandone.neberus.annotation.ApiType;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.test.request.SomeChildFieldDto;
import net.oneandone.neberus.test.request.SomeCtorDto;
import net.oneandone.neberus.test.request.SomeFieldDto;
import net.oneandone.neberus.test.request.SomeGetterDto;
import net.oneandone.neberus.test.response.Problem;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Class Documentation
 */
@ApiDocumentation
@Path("/rootPath")
@ApiLabel("Super Awesome REST Service")
@ApiHeaderDefinition(name = "header1", description = "description1")
@ApiHeaderDefinition(name = "header2", description = "description2")
@ApiHeaderDefinition(name = "Predefined", description = "one description to rule them all")
public class RestService {

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     *
     * @deprecated use this one {@link #justYetAnotherGetMethod(String, String, String, SomeFieldDto, SomeCtorDto, SomeChildFieldDto)}
     * or that one {@link #justYetAnotherGetMethod(String, String, String, SomeFieldDto, SomeCtorDto, SomeChildFieldDto)}
     * or even the one from the other resource {@link RestServiceWithInterfaceDoc#getMethod(String, String, String)}
     */
    @GET
    @Path("/anotherGet/{pathParam}/anotherPathParam/{anotherPathParam}/{wrappedPathParam}")
    @ApiLabel("This is an awesome method")
    @ApiDescription("Description of this awesomely awesome method defined in annotation!")
    @ApiResponse(status = ApiStatus.OK, description = "success")
    @ApiCurl
    @Deprecated
    public void justAnotherGetMethod(@PathParam("pathParam") @ApiAllowedValue("default") String pathParam,
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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiLabel("This is another awesome method")
    @ApiCurl
    @ApiResponse(status = ApiStatus.BAD_REQUEST, description = "Client Error", entities = {
            @ApiEntity(entityClass = net.oneandone.neberus.test.response.Problem.class, contentType = "application/problem+json")
    })
    @ApiResponse(status = ApiStatus.OK, description = "success", entities = {
            @ApiEntity(entityClass = Problem.class, contentType = "application/problem+json"),
            @ApiEntity(entityClass = SomeFieldDto.class, contentType = MediaType.APPLICATION_JSON)
    })
    @ApiParameter(name = "headerParam", description = "custom description <a href='index.html'>here</a>", type = ApiParameter.Type.HEADER)
    public void justYetAnotherGetMethod(@HeaderParam(HttpHeaders.AUTHORIZATION) @Size(max = 42) String header,
                                        @PathParam("pathParam")
                                        @ApiAllowedValue(value = "the", valueHint = "something like this")
                                        @ApiAllowedValue(value = "expected", valueHint = "something like that")
                                        @ApiAllowedValue(value = "allowed", valueHint = "or something like this")
                                        @ApiAllowedValue(value = "values", valueHint = "or something like that")
                                        @Size(max = 42) String pathParam,
                                        @ApiOptional @QueryParam("queryParam") @Size(max = 42) String queryParam,
                                        SomeFieldDto dto, SomeCtorDto otherDto, SomeChildFieldDto childDto) {

    }

    /**
     * Javadoc comment in asciidoc format.
     *
     * Level 1
     * -------
     * Text.
     *
     * Level 2
     * ~~~~~~~
     * Text.
     *
     * Level 3
     * ^^^^^^^
     * Text.
     *
     * Level 4
     * +++++++
     * Text.
     *
     * .Optional Title
     *
     * Usual
     * paragraph.
     *
     * .Optional Title
     * [source,perl]
     * ----
     * # *Source* block
     * # Use: highlight code listings
     * # (require `source-highlight` or `pygmentize`)
     * use DBI;
     * my $dbh = DBI->connect('...',$u,$p)
     *     or die "connect: $dbh->errstr";
     * ----
     *
     * normal, _italic_, *bold*, +mono+.
     *
     * ``double quoted'', `single quoted'.
     *
     * normal, ^super^, ~sub~.
     *
     * ....
     * Kismet: Where is the *defensive operations manual*?
     *
     * Computer: Calculating ...
     * Can not locate object.
     * You are not authorized to know it exists.
     *
     * Kismet: Did the werewolves tell you to say that?
     *
     * Computer: Calculating ...
     * ....
     */
    @PATCH
    @Path("{pathParamWithoutSlash}")
    @Consumes(MediaType.APPLICATION_XML)
    @ApiResponse(status = ApiStatus.OK, description = "success")
    @ApiCurl
    public void againAnotherGetMethod(SomeGetterDto dto, @PathParam("pathParamWithoutSlash") String pathparam) {

    }

    @POST
    @Path("/postFormParams")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiCurl
    @ApiResponse(status = ApiStatus.BAD_REQUEST, description = "bad request")
    @ApiResponse(status = ApiStatus.OK, description = "", entities = {
            @ApiEntity(entityClass = SomeFieldDto.class)
    })
    public Response postMethodWithForParams(@FormParam("param1") String formParam1,
                                            @FormParam("param2") @ApiType(String.class) WrappedString formParam2,
                                            @FormParam("param3") int formParam3) {
        return null;
    }

    public static class WrappedString {

        public String value;
    }

}
