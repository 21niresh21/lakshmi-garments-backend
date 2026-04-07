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
import com.lakshmigarments.dto.response.CategoryResponse;
import com.lakshmigarments.dto.response.SubCategoryResponse;
import com.lakshmigarments.service.CategoryService;
import com.lakshmigarments.service.InventoryService;

@RestController
@RequestMapping("/inventories")
@CrossOrigin(origins = "*")
public class InventoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryController.class);
	private final InventoryService inventoryService;
	private final CategoryService categoryService;

	public InventoryController(InventoryService inventoryService, CategoryService categoryService) {
		this.inventoryService = inventoryService;
		this.categoryService = categoryService;
	}

	@GetMapping
	public List<CategorySubCategoryCountDTO> getCategoryCount() {
		LOGGER.info("Received request to fetch category and subcategory counts.");
		List<CategorySubCategoryCountDTO> result = inventoryService.getCategorySubCategoryCounts();
		LOGGER.debug("Fetched {} category count records.", result.size());
		return result;
	}

	@GetMapping("/count")
	public Long getCategorySubCategoryCount(@RequestParam(name = "category-id") Long categoryId,
			@RequestParam(name = "subcategory-id") Long subCategoryId) {
		LOGGER.info("Received request to fetch count for category: '{}' and subCategory: '{}'", categoryId, subCategoryId);
		Long count = inventoryService.getCategorySubCategoryCount(categoryId, subCategoryId);
		LOGGER.debug("Fetched count: {} for category: '{}' and subCategory: '{}'", count, categoryId, subCategoryId);
		return count;
	}

	@GetMapping("/categories")	
	public List<CategoryResponse> getCategories() {
		LOGGER.info("Received request to fetch categories.");
		List<CategoryResponse> categories = categoryService.getAllCategories(null);
		LOGGER.debug("Fetched {} categories.", categories.size());
		return categories;
	}

	// get all subcategories for a given category
	@GetMapping("/sub-categories")
	public List<SubCategoryResponse> getSubCategories(@RequestParam(name = "category-id") Long categoryId) {
		LOGGER.info("Received request to fetch subcategories for category: '{}'", categoryId);
		List<SubCategoryResponse> subCategories = inventoryService.getSubCategories(categoryId);
		LOGGER.debug("Fetched {} subcategories for category: '{}'", subCategories.size(), categoryId);
		return subCategories;
	}
}
