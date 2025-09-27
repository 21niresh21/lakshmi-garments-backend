package com.lakshmigarments.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchDTO {
	
	private Long categoryID;
	
	private String serialCode;
		
	private Long batchStatusID;
	
	private Boolean isUrgent;
	
	private String remarks;
	
	private List<CreateBatchSubCategoryDTO> subCategories;
}
