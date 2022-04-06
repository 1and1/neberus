package net.oneandone.neberus.test.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.included.IncludedDto;
import com.notincluded.NotIncludedDto;
import net.oneandone.neberus.annotation.ApiAllowedValue;
import net.oneandone.neberus.annotation.ApiLabel;
import net.oneandone.neberus.annotation.ApiOptional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApiLabel("SomeGetterDtoLabel")
public class SomeGetterDto {

    private String stringField;
    private String stringFieldWithAllowedValue;
    private String stringFieldWithJavadoc;
    private String stringFieldWithEnumValuesFromSeeTag;
    private String stringFieldWithEnumValuesFromLinkTag;
    private String stringFieldWithEnumValuesFromApiAllowedValuesAnnotation;
    private String stringFieldWithNameFromJsonPropertyAnnotation;
    private String stringFieldWithConstraintPattern;
    private String stringFieldWithConstraintSize;
    private String stringFieldWithConstraintsFromSeeTag;
    private String stringFieldOptional;
    private String stringFieldWithJsonIgnoreAnnotation;
    private int intField;
    private float floatField;
    private double doubleField;
    private byte[] byteArrayField;
    private SomeEnum enumField;
    private Map<String, String> mapOfStringsField;
    private Set<String> setOfStringsField;
    private List<String> listOfStringsField;
    private String[] arrayOfStringsField;
    private NestedDto nestedDtoField;
    private IncludedDto includedDtoField;
    private NotIncludedDto notIncludedDtoField;
    private int intFieldWithConstraintsAndApiAllowedValuesAnnotation;
    private Map<String, String> mapFieldWithConstraintSize;
    private Map<String, Map<String, String>> mapFieldWithNestedMap;
    private List<List<String>> listFieldWithNestedList;
    private Set<Set<String>> setFieldWithNestedSet;
    private Map<String, List<Set<String>>> mapFieldWithNestedListWithNestedSet;
    private List<Map<String, Set<String>>> listFieldWithNestedMapWithNestedSet;
    private List<NestedDto> mapOfNestedDtoField;
    private String stringFieldDeprecated;
    private NestedGetterDto nestedGetterDto;

    public String getStringField() {
        return stringField;
    }

    @ApiAllowedValue(value = "the one and only allowed value", valueHint = "this is the actual one and only allowed value")
    public String getStringFieldWithAllowedValue() {
        return stringFieldWithAllowedValue;
    }

    /**
     * javadoc for stringFieldWithJavadoc
     */
    public String getStringFieldWithJavadoc() {
        return stringFieldWithJavadoc;
    }

    /**
     * javadoc for stringFieldWithEnumValuesFromSeeTag
     *
     * @see SomeEnum
     */
    public String getStringFieldWithEnumValuesFromSeeTag() {
        return stringFieldWithEnumValuesFromSeeTag;
    }

    /**
     * javadoc for stringFieldWithEnumValuesFromLinkTag {@link SomeEnum}
     */
    public String getStringFieldWithEnumValuesFromLinkTag() {
        return stringFieldWithEnumValuesFromLinkTag;
    }

    @ApiAllowedValue(enumValues = SomeEnum.class)
    public String getStringFieldWithEnumValuesFromApiAllowedValuesAnnotation() {
        return stringFieldWithEnumValuesFromApiAllowedValuesAnnotation;
    }

    /**
     * this is shadowed by the return tag
     *
     * @return this is the return tag text
     */
    @JsonProperty("I_AM_GETTERMAN")
    @Pattern(regexp = "abc.123")
    public String getStringFieldWithNameFromJsonPropertyAnnotation() {
        return stringFieldWithNameFromJsonPropertyAnnotation;
    }

    @Pattern(regexp = "abc.123")
    public String getStringFieldWithConstraintPattern() {
        return stringFieldWithConstraintPattern;
    }

    @Size(min = 0, max = 42)
    public String getStringFieldWithConstraintSize() {
        return stringFieldWithConstraintSize;
    }

    /**
     * @see IncludedDto#constraintReference
     */
    public String getStringFieldWithConstraintsFromSeeTag() {
        return stringFieldWithConstraintsFromSeeTag;
    }

    @ApiOptional
    public String getStringFieldOptional() {
        return stringFieldOptional;
    }

    @JsonIgnore
    public String getStringFieldWithJsonIgnoreAnnotation() {
        return stringFieldWithJsonIgnoreAnnotation;
    }

    public int getIntField() {
        return intField;
    }

    public float getFloatField() {
        return floatField;
    }

    public double getDoubleField() {
        return doubleField;
    }

    public byte[] getByteArrayField() {
        return byteArrayField;
    }

    public SomeEnum getEnumField() {
        return enumField;
    }

    public Map<String, String> getMapOfStringsField() {
        return mapOfStringsField;
    }

    public Set<String> getSetOfStringsField() {
        return setOfStringsField;
    }

    public List<String> getListOfStringsField() {
        return listOfStringsField;
    }

    public String[] getArrayOfStringsField() {
        return arrayOfStringsField;
    }

    public NestedDto getNestedDtoField() {
        return nestedDtoField;
    }

    public IncludedDto getIncludedDtoField() {
        return includedDtoField;
    }

    public NotIncludedDto getNotIncludedDtoField() {
        return notIncludedDtoField;
    }

    @ApiAllowedValue(value = "33", valueHint = "[-5, 42]")
    @Min(-5)
    @Max(42)
    public int getIntFieldWithConstraintsAndApiAllowedValuesAnnotation() {
        return intFieldWithConstraintsAndApiAllowedValuesAnnotation;
    }

    @Size(max = 42)
    public Map<String, String> getMapFieldWithConstraintSize() {
        return mapFieldWithConstraintSize;
    }

    public Map<String, Map<String, String>> getMapFieldWithNestedMap() {
        return mapFieldWithNestedMap;
    }

    public List<List<String>> getListFieldWithNestedList() {
        return listFieldWithNestedList;
    }

    public Set<Set<String>> getSetFieldWithNestedSet() {
        return setFieldWithNestedSet;
    }

    public Map<String, List<Set<String>>> getMapFieldWithNestedListWithNestedSet() {
        return mapFieldWithNestedListWithNestedSet;
    }

    public List<Map<String, Set<String>>> getListFieldWithNestedMapWithNestedSet() {
        return listFieldWithNestedMapWithNestedSet;
    }

    public List<NestedDto> getMapOfNestedDtoField() {
        return mapOfNestedDtoField;
    }

    /**
     * @deprecated this value is deprecated
     */
    @ApiOptional
    @Deprecated
    public String getStringFieldDeprecated() {
        return stringFieldDeprecated;
    }

    public NestedGetterDto getNestedGetterDto() {
        return nestedGetterDto;
    }
}
