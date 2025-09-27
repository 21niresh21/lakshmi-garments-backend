package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.CategorySubCategoryCountDTO;
import com.lakshmigarments.service.InventoryService;

@RestController
@RequestMapping("/inventories")
@CrossOrigin(origins = "*")
public class InventoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryController.class);
	private final InventoryService inventoryService;

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@GetMapping("/categories/subcategory-counts")
	public List<CategorySubCategoryCountDTO> getCategoryCount() {
		LOGGER.info("Received request to fetch category and subcategory counts.");
		List<CategorySubCategoryCountDTO> result = inventoryService.getCategorySubCategoryCounts();
		LOGGER.debug("Fetched {} category count records.", result.size());
		return result;
	}

	@GetMapping("/count")
	public Long getCategorySubCategoryCount(@RequestParam String category,
			@RequestParam(name = "subcategory") String subCategory) {
		LOGGER.info("Received request to fetch count for category: '{}' and subCategory: '{}'", category, subCategory);
		Long count = inventoryService.getCategorySubCategoryCount(category, subCategory);
		LOGGER.debug("Fetched count: {} for category: '{}' and subCategory: '{}'", count, category, subCategory);
		return count;
	}

}
