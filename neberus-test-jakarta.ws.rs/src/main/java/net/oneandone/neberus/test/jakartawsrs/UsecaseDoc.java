package net.oneandone.neberus.test.jakartawsrs;

import net.oneandone.neberus.annotation.ApiUsecase;
import net.oneandone.neberus.annotation.ApiUsecaseMethod;
import net.oneandone.neberus.annotation.ApiUsecaseParam;
import net.oneandone.neberus.annotation.ApiUsecaseRequestBody;
import net.oneandone.neberus.annotation.ApiUsecaseResponseBody;

import static net.oneandone.neberus.test.RestServiceInterfaceDoc.PATH_GET;
import static net.oneandone.neberus.test.RestServiceInterfaceDoc.PATH_GET_WITH_RESPONSES;
import static net.oneandone.neberus.test.RestServiceInterfaceDoc.PATH_POST;
import static net.oneandone.neberus.test.RestServiceInterfaceDoc.PATH_ROOT;

/**
 * This contains only usecases that are not trivial or require multiple calls.
 */
@ApiUsecase(name = "do it with hints", description = "just do it already",
            methods = {
                    @ApiUsecaseMethod(
                            path = PATH_ROOT + PATH_POST,
                            httpMethod = "GET",
                            description = "Call this first",
                            requestBody = @ApiUsecaseRequestBody(contentType = "application/json",
                                                                 valueHint = "the expected request body",
                                                                 value = "{\"key\":\"value\"}")),
                    @ApiUsecaseMethod(
                            path = PATH_ROOT + PATH_GET,
                            httpMethod = "GET",
                            description = "Then call this",
                            responseBody = @ApiUsecaseResponseBody(contentType = "application/json",
                                                                   valueHint = "and some hint",
                                                                   value = "{\"stringField\":\"with some value\"}"))
            })
@ApiUsecase(name = "do it without hints", description = "just do it already",
            methods = {
                    @ApiUsecaseMethod(
                            path = PATH_ROOT + PATH_GET_WITH_RESPONSES,
                            httpMethod = "GET",
                            description = "Call this first",
                            parameters = {
                                    @ApiUsecaseParam(name = "stringPathParam", value = "myId"),
                                    @ApiUsecaseParam(name = "queryParam123", value = "not my type")
                            }),
                    @ApiUsecaseMethod(
                            path = PATH_ROOT + PATH_GET,
                            httpMethod = "GET",
                            description = "Then call this",
                            responseBody = @ApiUsecaseResponseBody(contentType = "application/json",
                                                                   valueHint = "the expected request body",
                                                                   value = "{\"stringField\":\"with some value\"}"))
            })
public class UsecaseDoc {

}
