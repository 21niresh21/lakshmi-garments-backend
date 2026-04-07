package com.lakshmigarments.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.lakshmigarments.dto.response.BatchItemResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
	
	@Getter
	@Setter
	public static class BatchItemResponse {
		private Long id;
		private String itemName;
		private Long quantity;
	}
	
	private Long id;
	
	private String categoryName;
	
	private String serialCode;
	
	private String batchStatus;
	
	private Boolean isUrgent;
	
	private String remarks;
	
	private LocalDateTime createdAt;
	
	private String createdBy;
	
	private Long availableQuantity;
	
	private List<BatchSubCategoryResponseDTO> subCategories;
	
	private List<BatchItemResponse> items;

}
