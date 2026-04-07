package com.lakshmigarments.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lakshmigarments.model.BatchStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchRequestDTO {
	
	private String categoryName;
	
	private String serialCode;
		
	private BatchStatus batchStatus ;
	
	private Boolean isUrgent;
	
	private Long totalQuantity;
	
	private String remarks;
		
	private List<BatchSubCategoryRequestDTO> subCategories;
}
