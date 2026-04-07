package com.lakshmigarments.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateJobworkReceiptRequest {
	
	@NotNull(message = "Jobwork number required")
	private String jobworkNumber;
	
	@NotEmpty(message = "Jobwork Receipt Items list cannot be empty")
	private List<CreateJobworkReceiptItemRequest> jobworkReceiptItems;

}
