package net.oneandone.neberus.test.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.included.IncludedDto;
import com.notincluded.NotIncludedDto;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.oneandone.neberus.annotation.ApiAllowedValue;
import net.oneandone.neberus.annotation.ApiOptional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Set;

@XmlRootElement(name = "someFieldDtoRootName")
public class SomeFieldDto {

    public String stringField;

    @ApiAllowedValue(value = "the one and only allowed value", valueHint = "this is the actual one and only allowed value")
    public String stringFieldWithAllowedValue;

    /**
     * javadoc for stringFieldWithJavadoc
     */
    public String stringFieldWithJavadoc;

    /**
     * javadoc for stringFieldWithEnumValuesFromSeeTag
     *
     * @see SomeEnum
     */
    public String stringFieldWithEnumValuesFromSeeTag;

    /**
     * javadoc for stringFieldWithEnumValuesFromLinkTag {@link SomeEnum}
     */
    public String stringFieldWithEnumValuesFromLinkTag;

    @ApiAllowedValue(enumValues = SomeEnum.class)
    public String stringFieldWithEnumValuesFromApiAllowedValuesAnnotation;

    @JsonProperty("I_AM_FIELDMAN")
    public String stringFieldWithNameFromJsonPropertyAnnotation;

    @Pattern(regexp = "abc.123")
    public String stringFieldWithConstraintPattern;

    @Size(min = 0, max = 42)
    public String stringFieldWithConstraintSize;

    /**
     * @see IncludedDto#constraintReference
     */
    public String stringFieldWithConstraintsFromSeeTag;

    @ApiOptional
    public String stringFieldOptional;

    @JsonIgnore
    public String stringFieldWithJsonIgnoreAnnotation;

    public int intField;
    public float floatField;
    public double doubleField;
    public byte[] byteArrayField;
    public SomeEnum enumField;

    public Map<@ApiAllowedValue(valueHint = "map key value") String, @ApiAllowedValue(valueHint = "map value value") @Size(min = 5) String> mapOfStringsField;
    public Set<@ApiAllowedValue(enumValues = SomeEnum.class) String> setOfStringsField;
    public List<@ApiAllowedValue(valueHint = "list type argument value") @Size(min = 5) String> listOfStringsField;
    public String[] arrayOfStringsField;

    public NestedDto nestedDtoField;
    public IncludedDto includedDtoField;
    public NotIncludedDto notIncludedDtoField;

    @ApiAllowedValue(value = "33", valueHint = "[-5, 42]")
    @Min(-5)
    @Max(42)
    public int intFieldWithConstraintsAndApiAllowedValuesAnnotation;

    @Size(max = 42)
    public Map<String, String> mapFieldWithConstraintSize;

    public Map<String, Map<String, String>> mapFieldWithNestedMap;

    public List<List<String>> listFieldWithNestedList;

    public Set<Set<String>> setFieldWithNestedSet;

    public Map<String, List<Set<String>>> mapFieldWithNestedListWithNestedSet;
    public List<Map<String, Set<String>>> listFieldWithNestedMapWithNestedSet;

    public List<NestedDto> mapOfNestedDtoField;

    /**
     * @deprecated this value is deprecated
     */
    @ApiOptional
    @Deprecated
    public String stringFieldDeprecated;

}
