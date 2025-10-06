package com.lakshmigarments.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponseDTO {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BatchSubCategoryResponseDTO {
		
		private Long id;
		
		private String subCategoryName;
		
		private Long quantity;
	}
	
	private Long id;
	
	private String categoryName;
	
	private String serialCode;
	
	private String batchStatus;
	
	private Boolean isUrgent;
	
	private String remarks;
	
	private LocalDateTime createdAt;
	
	private List<BatchSubCategoryResponseDTO> subCategories;

}
