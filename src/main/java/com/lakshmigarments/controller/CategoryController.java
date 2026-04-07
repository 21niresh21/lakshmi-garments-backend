package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.request.CategoryRequest;
import com.lakshmigarments.dto.response.CategoryResponse;
import com.lakshmigarments.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

	private final CategoryService categoryService;

	@GetMapping
	public ResponseEntity<List<CategoryResponse>> getAllCategories(
			@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all categories matching: {}", search);
		List<CategoryResponse> categories = categoryService.getAllCategories(search);
		return ResponseEntity.ok(categories);
	}

	@PostMapping
	public ResponseEntity<CategoryResponse> createCategory(
			@RequestBody @Valid CategoryRequest request) {
		LOGGER.info("Received request to create category: {}", request.getName());
		CategoryResponse response = categoryService.createCategory(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
			@RequestBody @Valid CategoryRequest request) {
		LOGGER.info("Received request to update category ID: {}", id);
		CategoryResponse response = categoryService.updateCategory(id, request);
		return ResponseEntity.ok(response);
	}

}
