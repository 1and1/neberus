package net.oneandone.neberus.test;

import net.oneandone.neberus.annotation.ApiCommonResponse;
import net.oneandone.neberus.annotation.ApiDocumentation;
import net.oneandone.neberus.annotation.ApiHeaderDefinition;
import net.oneandone.neberus.model.ApiStatus;

@ApiDocumentation
@ApiHeaderDefinition(name = "header_1", description = "header description 1")
@ApiCommonResponse(status = ApiStatus.INTERNAL_SERVER_ERROR, description = "internal server error defined on class level")
public interface CommonRestServiceInterfaceDoc {

}
