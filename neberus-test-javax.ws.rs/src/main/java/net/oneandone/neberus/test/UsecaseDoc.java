package net.oneandone.neberus.test;

import net.oneandone.neberus.annotation.ApiUsecase;
import net.oneandone.neberus.annotation.ApiUsecaseMethod;
import net.oneandone.neberus.annotation.ApiUsecaseParam;
import net.oneandone.neberus.annotation.ApiUsecaseResponseValue;

/**
 * This contains only usecases that are not trivial or require multiple calls.
 */
@ApiUsecase(name = "do it with hints", description = "just do it already",
            methods = {
                @ApiUsecaseMethod(
                        name = "This is an awesome method",
                        restClass = RestServiceWithInterfaceDoc.class,
                        description = "Call this first",
                        parameters = {
                            @ApiUsecaseParam(name = "pathParam123", value = "myId", valueHint = "the hint"),
                            @ApiUsecaseParam(name = "queryParam123", valueHint = "not my type hint"),
                            @ApiUsecaseParam(name = "jsonParam", value = "bodyValue"),
                            @ApiUsecaseParam(name = "nestedDto.nestedString", value = "bodyValue")
                        }),
                @ApiUsecaseMethod(
                        name = "This is another awesome method",
                        restClass = RestServiceInterfaceDoc.class,
                        description = "Then call this",
                        responseValue = {
                            @ApiUsecaseResponseValue(name = "custom responseValue2", value = "with some value",
                                                     valueHint = "and some hint")
                        })
            })
@ApiUsecase(name = "do it without hints", description = "just do it already",
            methods = {
                @ApiUsecaseMethod(
                        name = "This is an awesome method",
                        restClass = RestServiceWithInterfaceDoc.class,
                        description = "Call this first",
                        parameters = {
                            @ApiUsecaseParam(name = "pathParam123", value = "myId"),
                            @ApiUsecaseParam(name = "queryParam123", value = "not my type")
                        }),
                @ApiUsecaseMethod(
                        name = "This is another awesome method",
                        restClass = RestServiceInterfaceDoc.class,
                        description = "Then call this",
                        responseValue = {
                            @ApiUsecaseResponseValue(name = "custom responseValue2", value = "with some value")
                        })
            })
public class UsecaseDoc {

}
