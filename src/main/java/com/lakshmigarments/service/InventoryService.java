package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CategorySubCategoryCountDTO;

@Service
public interface InventoryService {
	
	List<CategorySubCategoryCountDTO> getCategorySubCategoryCounts();

	Long getCategorySubCategoryCount(String category, String subCategory);

}
