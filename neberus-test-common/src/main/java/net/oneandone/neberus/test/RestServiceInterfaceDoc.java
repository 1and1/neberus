package net.oneandone.neberus.test;

import net.oneandone.neberus.annotation.ApiAllowedValue;
import net.oneandone.neberus.annotation.ApiCookie;
import net.oneandone.neberus.annotation.ApiCookieDefinition;
import net.oneandone.neberus.annotation.ApiCurl;
import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiEntity;
import net.oneandone.neberus.annotation.ApiExample;
import net.oneandone.neberus.annotation.ApiHeader;
import net.oneandone.neberus.annotation.ApiHeaderDefinition;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.annotation.ApiParameter;
import net.oneandone.neberus.annotation.ApiRequestEntity;
import net.oneandone.neberus.annotation.ApiResponse;
import net.oneandone.neberus.annotation.ApiType;
import net.oneandone.neberus.model.ApiStatus;
import net.oneandone.neberus.model.CookieSameSite;
import net.oneandone.neberus.test.request.SomeChildFieldDto;
import net.oneandone.neberus.test.request.SomeFieldDto;
import net.oneandone.neberus.test.request.SomeGetterDto;
import net.oneandone.neberus.test.response.Problem;
import net.oneandone.neberus.test.response.SuccessResponse;

import javax.validation.constraints.Size;

import java.util.UUID;

/**
 * REST Class Documentation
 */
@ApiLabel("REST Service with doc in interface")
@ApiDescription("Contains methods with ApiDoc defined in an interface.")
@ApiHeaderDefinition(name = "header_2", description = "header description 2")
@ApiCookieDefinition(name = "response-cookie_3", description = "descrption", sameSite = CookieSameSite.STRICT,
                     domain = "some.domain", path = "/somepath", httpOnly = true, secure = true, maxAge = "")
public interface RestServiceInterfaceDoc extends CommonRestServiceInterfaceDoc {

    String PATH_ROOT = "/root/restServiceInterfaceDoc";

    String PATH_PARAM_STRING = "stringPathParam";
    String PATH_PARAM_INT = "intPathParam";

    String PATH_GET = "/get";
    String PATH_GET_WITH_DISPLAYED_INTERNAL_DOC = "/get/withInternalDoc";
    String PATH_GET_WITH_RESPONSES = "/get/withParams/{" + PATH_PARAM_STRING + "}/subpath/{" + PATH_PARAM_INT + "}";

    String PATH_PUT = "/put";
    String PATH_POST = "/post";
    String PATH_DELETE = "/delete";

    @ApiLabel("GET method with interface doc")
    @ApiDescription("Description of 'GET method with interface doc' defined in annotation!")
    @ApiCurl
    @ApiParameter(name = "some-cookie", description = "cookie description", type = ApiParameter.Type.COOKIE)
    @ApiResponse(status = ApiStatus.OK, description = "Successful response",
                 entities = @ApiEntity(entityClass = SuccessResponse.class, description = "Success response annotation doc"),
                 cookies = {
                         @ApiCookie(name = "response-cookie_1", description = "description", sameSite = CookieSameSite.STRICT,
                                    domain = "some.domain", path = "/somepath", httpOnly = true, secure = true),
                         @ApiCookie(name = "response-cookie_2", description = "description"),
                         @ApiCookie(name = "response-cookie_3"),
                 })
    @ApiResponse(status = ApiStatus.CREATED, description = "Successful response",
                 entities = @ApiEntity(entityClass = SuccessResponse.class))
    Object getMethod();

    Object getMethodWithDisplayedInternalDoc(String queryParam);

    @ApiLabel("PUT method with interface doc")
    @ApiResponse(status = ApiStatus.NO_CONTENT, description = "Success")
    @ApiResponse(status = ApiStatus.INTERNAL_SERVER_ERROR, description = "internal server error defined on the method")
    void putMethod(SomeFieldDto dto);

    @ApiLabel("POST method with interface doc")
    @ApiResponse(status = ApiStatus.OK, description = "Warnings response",
                 entities = {
                         @ApiEntity(entityClass = Problem[].class, description = "a problem",
                                    contentType = "application/warnings+json", examples = {
                                 @ApiExample(title = "example warning #1",
                                             value = "[{\"title\":\"some title example\"}]"),
                                 @ApiExample(title = "example warning #2",
                                             value = "[{\"title\":\"some other title example\"}]")
                         })
                 })
    @ApiRequestEntity(entityClass = SomeChildFieldDto.class, contentType = "application/xml", description = "special dto for xml",
                      examples = {
                              @ApiExample(title = "some example", value = "example value")
                      })
    void postMethod(SomeFieldDto dto);

    @ApiLabel("DELETE method with interface doc")
    @ApiCurl
    @ApiResponse(status = ApiStatus.NO_CONTENT, description = "Success")
    void deleteMethod();

    /**
     * Description of 'GET method with interface doc' defined as javadoc!<br>
     * With a link to {@link #putMethod(SomeFieldDto)}.
     * With a html newline and <strong>strong text</strong>.
     * <br>
     * This is a line
     * that should not have newlines in apidoc, but a newline after.
     * <p>
     * This line should only have one newline after, and not two...<br>
     * <p>
     * ...until this line.
     *
     * @param stringPathParam  param doc
     * @param anotherPathParam param `doc`
     *
     * @return some restdoc
     */
    @ApiLabel("GET method with interface doc and stuff -.,#'+/()=}]{[!\"§$%&")
    @ApiResponse(status = ApiStatus.NO_CONTENT, description = "Nothing found")
    @ApiResponse(status = ApiStatus.OK, description = "Successful response\nwith a multi-line\ndescription",
                 entities = {
                         @ApiEntity(entityClass = SomeFieldDto.class, description = "standard response", examples = {
                                 @ApiExample(title = "example response", value = "{\"jsonParam\":\"some value example\"}"),
                                 @ApiExample(title = "other example response", value = "{\"jsonParam\":\"some other value example\"}")
                         }),
                         @ApiEntity(entityClass = SomeGetterDto.class, description = "alternate response", contentType = "application/xml", examples = {
                                 @ApiExample(title = "example response", value = "{\"jsonParam\":\"some value example\"}"),
                         })
                 })
    @ApiResponse(status = ApiStatus.BAD_GATEWAY, description = "a bad gateways response")
    @ApiResponse(status = ApiStatus.MOVED_PERMANENTLY, description = "a redirect response")
    @ApiResponse(status = ApiStatus.BAD_REQUEST, description = "Client error",
                 entities = {
                         @ApiEntity(entityClass = Problem.class, examples = {
                                 @ApiExample(title = "example response", value = "{\"title\":\"some title example\"}")
                         })
                 },
                 headers = {
                         @ApiHeader(name = "123", description = "456"),
                         @ApiHeader(name = "header_2")
                 })
    @ApiCurl
    Object getMethod(
            @ApiAllowedValue(value = "default", valueHint = "the default value")
            @ApiAllowedValue(value = "not-default")
            String stringPathParam,
            @Size(max = 42) String anotherPathParam,
            @ApiType(UUID.class) @ApiAllowedValue(valueHint = "queryParam value hint") String queryParam);

}
