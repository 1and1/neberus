package net.oneandone.neberus.test.request;

import net.oneandone.neberus.annotation.ApiOptional;

public class NestedCtorDto {

    private String valueFromNested;
    private NestedNestedDto nestedNestedDto;

    public NestedCtorDto(@ApiOptional String valueFromNested, NestedNestedDto nestedNestedDto) {
        this.valueFromNested = valueFromNested;
        this.nestedNestedDto = nestedNestedDto;
    }
}
