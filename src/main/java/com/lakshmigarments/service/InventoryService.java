package com.lakshmigarments.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CategoryCountDTO;
import com.lakshmigarments.dto.SubCategoryCountDTO;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.repository.InventoryRepository;

@Service
public class InventoryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryService.class);
	private InventoryRepository warehouseRepository;
	
	public InventoryService(InventoryRepository warehouseRepository) {
		this.warehouseRepository = warehouseRepository;
	}
	
	public List<CategoryCountDTO> getCategorySubCategoryCounts() {
	    List<CategoryCountDTO> categoryCountDTOs = new ArrayList<>();
	    List<Object[]> result = warehouseRepository.getCategorySubCategoryCount();

	    Map<Category, CategoryCountDTO> categoryMap = new HashMap();

	    for (Object[] row : result) {
	        Category category = (Category) row[0];
	        String subCategoryName = (String) row[1];
	        Long totalCount = (Long) row[2];

	        // Fetch or create the DTO for the category
	        CategoryCountDTO categoryDTO = categoryMap.computeIfAbsent(category, k -> {
	            CategoryCountDTO newDTO = new CategoryCountDTO();
	            newDTO.setCategory(category);
	            newDTO.setSubCategoryCountDTOs(new ArrayList<>());
	            return newDTO;
	        });

	        // Add subcategory details
	        categoryDTO.getSubCategoryCountDTOs().add(new SubCategoryCountDTO(subCategoryName, totalCount));
	    }

	    return new ArrayList<>(categoryMap.values());
	}

	
	public Inventory getCategorySubCategoryCount(String category, String subCategory) {
		Inventory warehouse = warehouseRepository.findBySubCategoryName(subCategory).orElse(null);
		if (warehouse == null) {
			warehouse = new Inventory();
			warehouse.setCount(0);
		}
		return warehouse;
	}
	
    public Page<Inventory> getInventories(String category, String subCategory, int page, int size) {
        return warehouseRepository.findByCategoryAndSubCategory(category, subCategory, PageRequest.of(page, size));
    }
}