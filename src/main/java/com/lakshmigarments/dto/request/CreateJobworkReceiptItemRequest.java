package com.lakshmigarments.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateJobworkReceiptItemRequest {

	@NotBlank(message = "Item name required")
	private String itemName;
	
	private Long acceptedQuantity = 0L;
	private Long salesQuantity = 0L;
	private Double salesPrice = 0.0;
	private Double wagePerItem = 0.0;
	private List<CreateDamageRequest> damages;
	
}
