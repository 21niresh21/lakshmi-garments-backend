package com.lakshmigarments.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateItemBasedJobworkRequest extends CreateJobworkRequest {

	@NotEmpty
	private List<String> itemNames;
	
	@NotEmpty
	private List<Long> quantities;
}
