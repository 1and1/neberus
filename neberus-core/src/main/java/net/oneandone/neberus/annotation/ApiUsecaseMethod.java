package net.oneandone.neberus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A specific REST method used in an usecase.
 * If the method is documented within the same service, it can be referenced by providing the restClass and name (label) of the
 * method. In this case a link will be created and all parameters and responseValues will be cross-checked so they actually
 * exist in the documented method.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface ApiUsecaseMethod {

    /**
     * Path of the method to call. Existing methods with matching path and httpMethod will be linked.
     *
     * @return the path
     */
    String path();

    /**
     * HttpMethod to use with the provided path.
     *
     * @return the httpMethod
     */
    String httpMethod();

    /**
     * Description of the usage of this method.
     *
     * @return the description
     */
    String description();

    /**
     * Usage explanation of the parameters (header|query|path).
     *
     * @return the parameters
     */
    ApiUsecaseParam[] parameters() default {};

    /**
     * Custom example of the request body for the usecase.
     *
     * @return the request body
     */
    ApiUsecaseRequestBody[] requestBody() default {};

    /**
     * Custom example of the response body for the usecase.
     *
     * @return the response body
     */
    ApiUsecaseResponseBody[] responseBody() default {};

}
