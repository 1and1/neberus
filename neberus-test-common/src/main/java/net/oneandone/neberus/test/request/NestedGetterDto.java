package net.oneandone.neberus.test.request;

import net.oneandone.neberus.annotation.ApiOptional;

public class NestedGetterDto {

    private String valueFromNested;
    private NestedNestedDto nestedNestedDto;

    @ApiOptional
    public String getValueFromNested() {
        return valueFromNested;
    }

    public void setValueFromNested(String valueFromNested) {
        this.valueFromNested = valueFromNested;
    }

    public NestedNestedDto getNestedNestedDto() {
        return nestedNestedDto;
    }

    public void setNestedNestedDto(NestedNestedDto nestedNestedDto) {
        this.nestedNestedDto = nestedNestedDto;
    }
}
