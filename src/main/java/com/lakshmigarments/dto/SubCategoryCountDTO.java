package com.lakshmigarments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class SubCategoryCountDTO {

	private String subCategoryName;
	private Long count;
	private Double percentageOfCategory;

}
