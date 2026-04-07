package com.lakshmigarments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for creating or updating supplier information.
 * Contains validation constraints to ensure data integrity.
 */
@Getter
@Setter
public class SupplierRequest {

    @NotBlank(message = "Supplier name is mandatory")
    @Size(max = 200, message = "Supplier name must be less than 200 characters")
    private String name;

    @NotBlank(message = "Supplier location is mandatory")
    @Size(max = 400, message = "Supplier location must be less than 400 characters")
    private String location;

}
