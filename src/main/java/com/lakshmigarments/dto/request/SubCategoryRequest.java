package com.lakshmigarments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubCategoryRequest {

    @NotBlank(message = "Sub category name is mandatory")
    @Size(max = 200, message = "Sub category name should be less than 200 characters")
    private String name;

}
