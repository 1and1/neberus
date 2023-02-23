package net.oneandone.neberus.test.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SomeCtorAndGetterDto {

    private String stringFieldWithGetterAnnotation;
    private String stringFieldWithCtorAnnotation;

    public SomeCtorAndGetterDto(String stringFieldWithGetterAnnotation,
            @JsonProperty("ctorParameterName") String stringFieldWithCtorAnnotation) {
        this.stringFieldWithGetterAnnotation = stringFieldWithGetterAnnotation;
        this.stringFieldWithCtorAnnotation = stringFieldWithCtorAnnotation;
    }

    @JsonProperty("getterFieldName")
    public String getStringFieldWithGetterAnnotation() {
        return stringFieldWithGetterAnnotation;
    }

    public String getStringFieldWithCtorAnnotation() {
        return stringFieldWithCtorAnnotation;
    }

}
