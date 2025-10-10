package com.lakshmigarments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubCategoryRequestDTO {

    @NotNull(message = "Sub category name is mandatory")
    @NotBlank(message = "Sub category name is mandatory")
    @Size(max = 200, message = "Sub category name should be less than 200 characters")
    private String name;

}
