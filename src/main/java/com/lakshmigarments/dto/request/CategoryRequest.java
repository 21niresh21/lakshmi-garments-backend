package com.lakshmigarments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Category name is mandatory")
    @Size(max = 200, message = "Category name should be less than 200 characters")
    private String name;

    @NotBlank(message = "Category code is mandatory")
    @Size(max = 10, message = "Category code should be less than 10 characters")
    private String code;
    
}
