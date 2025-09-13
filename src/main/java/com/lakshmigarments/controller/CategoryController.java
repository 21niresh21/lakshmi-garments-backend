package com.lakshmigarments.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.CreateCategoryDTO;
import com.lakshmigarments.dto.UpdateCategoryDTO;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.service.CategoryService;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);
	private final CategoryService categoryService;
	
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	@PostMapping
	public ResponseEntity<Category> createCategory(@RequestBody @Validated CreateCategoryDTO createCategoryDTO) {
		LOGGER.info("Create a new category");
		return new ResponseEntity<>(categoryService.createCategory(createCategoryDTO), HttpStatus.CREATED);
	}
	
	@GetMapping
	public ResponseEntity<Page<Category>> getCategories(
			@RequestParam(defaultValue = "0", required = false) Integer pageNo,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "id", required = false) String sortBy,
			@RequestParam(defaultValue = "asc", required = false) String sortDir) {
		Page<Category> categoryPage = categoryService.getCategories(pageNo, pageSize, sortBy, sortDir, search);
		LOGGER.info("Retrieve categories");
		return new ResponseEntity<Page<Category>>(categoryPage, HttpStatus.OK);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<Category> updateCategory(
	        @PathVariable Long id,
	        @RequestBody @Validated UpdateCategoryDTO updateCategoryDTO) {

	    LOGGER.info("Updating category with ID: {}", id);
	    Category updatedCategory = categoryService.updateCategory(id, updateCategoryDTO);
	    LOGGER.info("Updated category with ID: {}", id);

	    return ResponseEntity.ok(updatedCategory);
	}

	
}
