package net.oneandone.neberus.test.request;

import net.oneandone.neberus.annotation.ApiType;

/**
 * class javadoc from SomeChildRecordDto
 */
public record SomeChildRecordDto(
        @ApiType(boolean.class)
        String stringFieldFromChildWithApiTypeBoolean
) {
}
