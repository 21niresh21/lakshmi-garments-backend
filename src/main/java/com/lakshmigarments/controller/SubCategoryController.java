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

import com.lakshmigarments.dto.request.SubCategoryRequest;
import com.lakshmigarments.dto.response.SubCategoryResponse;
import com.lakshmigarments.service.SubCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sub-categories")
@RequiredArgsConstructor
public class SubCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubCategoryController.class);

	private final SubCategoryService subCategoryService;

	@GetMapping
	public ResponseEntity<List<SubCategoryResponse>> getAllSubCategories(
			@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all subcategories matching: {}", search);
		List<SubCategoryResponse> subCategories = subCategoryService.getAllSubCategories(search);
		return ResponseEntity.ok(subCategories);
	}

	@PostMapping
	public ResponseEntity<SubCategoryResponse> createSubCategory(@RequestBody @Valid SubCategoryRequest request) {
		LOGGER.info("Received request to create subcategory: {}", request.getName());
		SubCategoryResponse response = subCategoryService.createSubCategory(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SubCategoryResponse> updateSubCategory(@PathVariable Long id,
			@RequestBody @Valid SubCategoryRequest request) {
		LOGGER.info("Received request to update subcategory ID: {}", id);
		SubCategoryResponse response = subCategoryService.updateSubCategory(id, request);
		return ResponseEntity.ok(response);
	}

}
