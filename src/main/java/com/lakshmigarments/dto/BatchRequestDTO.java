package com.lakshmigarments.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchRequestDTO {
	
	private Long categoryID;
	
	private String serialCode;
		
	private Long batchStatusID;
	
	private Boolean isUrgent;
	
	private String remarks;
	
	private List<BatchSubCategoryRequestDTO> subCategories;
}
