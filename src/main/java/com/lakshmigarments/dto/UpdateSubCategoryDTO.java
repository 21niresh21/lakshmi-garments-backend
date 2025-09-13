package com.lakshmigarments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubCategoryDTO {

	private String name;
    private Long categoryId; 
	
}
