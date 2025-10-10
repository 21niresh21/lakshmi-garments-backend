package com.lakshmigarments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequestDTO {

	@NotBlank(message = "Supplier name is mandatory")
	@Size(max = 200, message = "Supplier name must be less than 200 characters")
	private String name;
	
	@NotBlank(message = "Supplier location is mandatory")
	@Size(max = 200, message = "Supplier location must be less than 200 characters")
	private String location;
}
