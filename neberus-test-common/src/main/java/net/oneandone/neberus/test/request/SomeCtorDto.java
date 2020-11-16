package net.oneandone.neberus.test.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.included.IncludedDto;
import com.notincluded.NotIncludedDto;
import net.oneandone.neberus.annotation.ApiAllowedValue;
import net.oneandone.neberus.annotation.ApiIgnore;
import net.oneandone.neberus.annotation.ApiOptional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SomeCtorDto {

    /**
     * @param stringParam
     * @param stringParamWithAllowedValue
     * @param stringParamWithJavadoc                                  javadoc for stringParamWithJavadoc
     * @param stringParamWithEnumValuesFromSeeTag                     javadoc for stringParamWithEnumValuesFromSeeTag
     * @param stringParamWithEnumValuesFromLinkTag                    javadoc for stringParamWithEnumValuesFromLinkTag {@link SomeEnum}
     * @param stringParamWithEnumValuesFromApiAllowedValuesAnnotation
     * @param stringParamWithNameFromJsonPropertyAnnotation
     * @param stringParamWithConstraintPattern
     * @param stringParamWithConstraintSize
     * @param stringParamWithConstraintsFromSeeTag                    @see IncludedDto#constraintReference
     * @param stringParamOptional
     * @param stringParamWithJsonIgnoreAnnotation
     * @param intParam
     * @param floatParam
     * @param doubleParam
     * @param byteArrayParam
     * @param enumParam
     * @param mapOfStringsParam
     * @param setOfStringsParam
     * @param listOfStringsParam
     * @param arrayOfStringsParam
     * @param nestedDtoParam
     * @param includedDtoParam
     * @param notIncludedDtoParam
     * @param intParamWithConstraintsAndApiAllowedValuesAnnotation
     * @param mapParamWithConstraintSize
     * @param mapParamWithNestedMap
     * @param listParamWithNestedList
     * @param setParamWithNestedSet
     * @param mapParamWithNestedListWithNestedSet
     * @param listParamWithNestedMapWithNestedSet
     * @param mapOfNestedDtoParam
     */
    public SomeCtorDto(String stringParam,
                       @ApiAllowedValue(value = "the one and only allowed value", valueHint = "this is the actual one and only allowed value")
                               String stringParamWithAllowedValue,
                       String stringParamWithJavadoc,
                       String stringParamWithEnumValuesFromSeeTag,
                       String stringParamWithEnumValuesFromLinkTag,
                       @ApiAllowedValue(enumValues = SomeEnum.class)
                               String stringParamWithEnumValuesFromApiAllowedValuesAnnotation,
                       @JsonProperty("I_AM_PARAMMAN")
                               String stringParamWithNameFromJsonPropertyAnnotation,
                       @Pattern(regexp = "abc.123")
                               String stringParamWithConstraintPattern,
                       @Size(min = 0, max = 42)
                               String stringParamWithConstraintSize,
                       String stringParamWithConstraintsFromSeeTag,
                       @ApiOptional
                               String stringParamOptional,
                       @ApiIgnore
                               String stringParamWithJsonIgnoreAnnotation,
                       int intParam,
                       float floatParam,
                       double doubleParam,
                       byte[] byteArrayParam,
                       SomeEnum enumParam,
                       Map<String, String> mapOfStringsParam,
                       Set<String> setOfStringsParam,
                       List<String> listOfStringsParam,
                       String[] arrayOfStringsParam,
                       NestedDto nestedDtoParam,
                       IncludedDto includedDtoParam,
                       NotIncludedDto notIncludedDtoParam,
                       @ApiAllowedValue(value = "33", valueHint = "[-5, 42]")
                       @Min(-5)
                       @Max(42)
                               int intParamWithConstraintsAndApiAllowedValuesAnnotation,
                       @Size(max = 42)
                               Map<String, String> mapParamWithConstraintSize,
                       Map<String, Map<String, String>> mapParamWithNestedMap,
                       List<List<String>> listParamWithNestedList,
                       Set<Set<String>> setParamWithNestedSet,
                       Map<String, List<Set<String>>> mapParamWithNestedListWithNestedSet,
                       List<Map<String, Set<String>>> listParamWithNestedMapWithNestedSet,
                       List<NestedDto> mapOfNestedDtoParam,
                       @Deprecated @ApiOptional String stringParamDeprecated) {
    }
}
