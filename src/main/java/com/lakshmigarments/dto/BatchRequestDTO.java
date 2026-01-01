package com.lakshmigarments.dto;

import java.util.List;

import com.lakshmigarments.model.BatchStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchRequestDTO {
	
	private String categoryName;
	
	private String serialCode;
		
	private BatchStatus batchStatus ;
	
	private Boolean isUrgent;
	
	private Long totalQuantity;
	
	private String remarks;
	
	private Long createdByID;
	
	private List<BatchSubCategoryRequestDTO> subCategories;
}
