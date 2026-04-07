package com.lakshmigarments.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateBaleDTO {
	
	@NotBlank(message = "Bale number is mandatory")
	@Size(max = 100)
	private String baleNumber;
	
	@Positive(message = "Quantity should be positive")
	@NotNull(message = "Quantity is mandatory")
	private Long quantity;
	
	@Positive(message = "Length should be positive")
	@NotNull(message = "Length is mandatory")
	private Double length;
	
	@Positive(message = "Price should be positive")
	@NotNull(message = "Price is mandatory")
	private Double price;
	
	@NotBlank(message = "Quality is mandatory")
	@Size(max = 200)
	private String quality;
	
	@NotNull(message = "Sub category is mandatory")
	private Long subCategoryID;

	@NotNull(message = "Category is mandatory")
	private Long categoryID;
}
