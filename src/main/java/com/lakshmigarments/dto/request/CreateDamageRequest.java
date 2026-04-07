package com.lakshmigarments.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDamageRequest {
	
	@NotBlank(message = "Damage type required")
	private String type;
	private Long quantity = 0L;

	// Source is only required for REPAIRABLE damage type
	private String source;
	
	// Rework jobwork number (used when damage is from PREVIOUS_JOBWORK and needs to be assigned to a specific jobwork for rework)
	private String reworkJobworkNumber;
	
	// Jobwork number from which the damage was reported (optional, defaults to current jobwork if not provided)
	private String reportedJobworkFrom;

}
