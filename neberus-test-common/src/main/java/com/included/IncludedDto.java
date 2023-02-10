package com.included;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class IncludedDto {

    public String documented;

    @Size(max = 42)
    @Pattern(regexp = "hallo.*")
    public String constraintReference;

}
