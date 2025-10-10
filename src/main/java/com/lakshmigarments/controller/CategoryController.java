package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.lakshmigarments.dto.CategoryRequestDTO;
import com.lakshmigarments.dto.CategoryResponseDTO;
import com.lakshmigarments.service.CategoryService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class CategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);
	private final CategoryService categoryService;

	@PostMapping
	public ResponseEntity<CategoryResponseDTO> createCategory(
			@RequestBody @Validated CategoryRequestDTO createCategoryDTO) {
		LOGGER.info("Received request to create a new category: {}", createCategoryDTO.getName());

		CategoryResponseDTO categoryResponseDTO = categoryService.createCategory(createCategoryDTO);

		LOGGER.info("Category created successfully with ID: {}", categoryResponseDTO.getId());

		return new ResponseEntity<>(categoryResponseDTO, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all categories with search: {}", search);
		List<CategoryResponseDTO> categories = categoryService.getAllCategories(search);
		LOGGER.info("Returning {} category(s)", categories.size());
		return ResponseEntity.ok(categories);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<CategoryResponseDTO> updateCategory(
			@PathVariable Long id,
			@RequestBody @Valid CategoryRequestDTO updateCategoryDTO) {

		LOGGER.info("Updating category with ID: {}", id);
		CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, updateCategoryDTO);
		LOGGER.info("Updated category with ID: {}", id);

		return ResponseEntity.ok(updatedCategory);
	}

}
