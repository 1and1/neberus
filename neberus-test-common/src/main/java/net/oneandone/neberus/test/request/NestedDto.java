package net.oneandone.neberus.test.request;

import net.oneandone.neberus.annotation.ApiOptional;

public class NestedDto {

    @ApiOptional
    public String valueFromNested;
    public NestedNestedDto nestedNestedDto;
}
