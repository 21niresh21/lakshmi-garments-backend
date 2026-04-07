package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CategorySubCategoryCountDTO;
import com.lakshmigarments.dto.response.SubCategoryResponse;

@Service
public interface InventoryService {
	
	List<CategorySubCategoryCountDTO> getCategorySubCategoryCounts();

	Long getCategorySubCategoryCount(Long categoryId, Long subCategoryId);
	
	List<SubCategoryResponse> getSubCategories(Long categoryId);

}
