package com.lakshmigarments.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateLorryReceiptDTO {
	
	@NotBlank(message = "LR number is mandatory")
	@Size(max = 100)
	@JsonProperty("lrNumber")
	private String lrNumber;
	
	private List<CreateBaleDTO> bales;
}
