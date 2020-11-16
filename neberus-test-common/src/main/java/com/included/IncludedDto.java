package com.included;


import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class IncludedDto {

    public String documented;

    @Size(max = 42)
    @Pattern(regexp = "hallo.*")
    public String constraintReference;

}
