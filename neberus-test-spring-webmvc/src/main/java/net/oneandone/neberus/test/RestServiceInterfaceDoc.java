package net.oneandone.neberus.test;

import net.oneandone.neberus.annotation.ApiCurl;
import net.oneandone.neberus.annotation.ApiDescription;
import net.oneandone.neberus.annotation.ApiDocumentation;
import net.oneandone.neberus.annotation.ApiHeader;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.annotation.ApiParameter;
import net.oneandone.neberus.annotation.ApiProblemResponse;
import net.oneandone.neberus.annotation.ApiResponseValue;
import net.oneandone.neberus.annotation.ApiSuccessResponse;
import net.oneandone.neberus.annotation.ApiWarning;
import net.oneandone.neberus.annotation.ApiWarningResponse;
import net.oneandone.neberus.model.ProblemType;
import net.oneandone.neberus.test.RestServiceWithInterfaceDoc.SomeDto;
import net.oneandone.neberus.annotation.ApiAllowedValues;
import net.oneandone.neberus.annotation.ApiType;
import net.oneandone.neberus.model.ApiStatus;
import java.util.UUID;

import org.springframework.http.MediaType;

/**
 * REST Class Documentation
 */
@ApiDocumentation
@ApiHeader(name = "header1", description = "description1")
@ApiHeader(name = "header2", description = "description2")
public interface RestServiceInterfaceDoc {

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     *
     * @param pathParamInterfaceName restdoc
     * @deprecated use the other one
     *
     * @return some restdoc
     */
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
    SomeDto justAnotherGetMethodWithInterfaceDoc(@ApiAllowedValues("default") String pathParamInterfaceName,
                                                         String anotherPathParam,
                                                         @ApiType(UUID.class) String queryParam);

    /**
     * ApiDescription of this awesomely awesome method defined as javadoc!
     * @param pathParam path doc in interface
     */
    @ApiLabel("This is another awesome method")
    @ApiCurl
    @ApiSuccessResponse(status = ApiStatus.BAD_REQUEST)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = FailureResultDto.class, contentType = MediaType.APPLICATION_JSON_VALUE)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = SomeDto.class, contentType = MediaType.APPLICATION_JSON_VALUE)
    @ApiParameter(name = "jsonParam", description = "custom description", containerClass = SomeDto.class)
    @ApiResponseValue(name = "jsonParam2", description = "custom description", containerClass = SomeDto.class)
    @ApiResponseValue(name = "custom responseValue2", description = "custom description")
    void justYetAnotherGetMethodWithInterfaceDoc(
            @ApiAllowedValues("the expected default value") String pathParam,
            String queryParam);

}
