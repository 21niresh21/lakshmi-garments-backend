package com.lakshmigarments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchSubCategoryDTO {
	
	private Long subCategoryID;
	
	private Long quantity;
}
