package net.oneandone.neberus.test.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.included.IncludedDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.oneandone.neberus.annotation.ApiAllowedValue;
import net.oneandone.neberus.annotation.ApiOptional;
import net.oneandone.neberus.annotation.ApiType;

import java.util.Set;

/**
 * record dto class doc
 *
 * @param stringFieldWithJavadoc                javadoc for stringFieldWithJavadoc
 * @param stringFieldWithEnumValuesFromLinkTag  javadoc for stringFieldWithEnumValuesFromLinkTag {@link SomeEnum}
 * @param stringFieldWithConstraintsFromLinkTag {@link IncludedDto#constraintReference}
 * @param stringFieldDeprecated                 this value is deprecated
 * @param someChildRecordDtoWithOverrideDoc     doc from param tag
 */
@XmlRootElement(name = "someFieldDtoRootName")
public record SomeRecordDto(
        String stringField,

        @ApiAllowedValue(value = "the one and only allowed value", valueHint = "this is the actual one and only allowed value")
        String stringFieldWithAllowedValue,

        String stringFieldWithJavadoc,
        String stringFieldWithEnumValuesFromLinkTag,

        @ApiAllowedValue(enumValues = SomeEnum.class)
        String stringFieldWithEnumValuesFromApiAllowedValuesAnnotation,

        @JsonProperty("ns:I_AM_FIELDMAN")
        String stringFieldWithNameFromJsonPropertyAnnotation,

        @Pattern(regexp = "abc.123")
        String stringFieldWithConstraintPattern,

        @Size(min = 0, max = 42)
        String stringFieldWithConstraintSize,

        String stringFieldWithConstraintsFromLinkTag,

        @ApiOptional
        String stringFieldOptional,

        @JsonIgnore
        String stringFieldWithJsonIgnoreAnnotation,

        @ApiAllowedValue(value = "33", valueHint = "[-5, 42]")
        @Min(-5)
        @Max(42)
        int intFieldWithConstraintsAndApiAllowedValuesAnnotation,

        @ApiOptional
        @Deprecated
        String stringFieldDeprecated,

        Set<String> setField,

        SomeChildRecordDto someChildRecordDto,

        SomeChildRecordDto someChildRecordDtoWithOverrideDoc,

        @ApiType(String.class)
        SomeChildRecordDto fieldWithApiType
) {

}
