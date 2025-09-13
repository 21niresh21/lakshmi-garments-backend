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

import com.lakshmigarments.dto.CreateSubCategoryDTO;
import com.lakshmigarments.dto.UpdateSubCategoryDTO;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.service.SubCategoryService;

@RestController
@RequestMapping("/sub-categories")
@CrossOrigin(origins = "*")
public class SubCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubCategoryController.class);
	private final SubCategoryService subCategoryService;
	
	public SubCategoryController(SubCategoryService subCategoryService) {
		this.subCategoryService = subCategoryService;
	}
	
	@PostMapping
	public ResponseEntity<SubCategory> createSubCategory(@RequestBody @Validated CreateSubCategoryDTO createSubCategoryDTO) {
		LOGGER.info("Create a new sub category");
		return new ResponseEntity<>(subCategoryService.createSubCategory(createSubCategoryDTO), HttpStatus.CREATED);
	}
	
	@GetMapping
	public ResponseEntity<Page<SubCategory>> getSubCategories(
			@RequestParam(defaultValue = "0", required = false) Integer pageNo,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "id", required = false) String sortBy,
			@RequestParam(defaultValue = "asc", required = false) String sortDir) {
		Page<SubCategory> subCategoryPage = subCategoryService.getSubCategories(pageNo, pageSize, sortBy, sortDir, search);
		LOGGER.info("Retrieve sub categories");
		return new ResponseEntity<Page<SubCategory>>(subCategoryPage, HttpStatus.OK);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<SubCategory> updateSubCategory(
	        @PathVariable Long id,
	        @RequestBody @Validated UpdateSubCategoryDTO dto) {

	    LOGGER.info("Updating SubCategory with ID: {}", id);
	    SubCategory updatedSubCategory = subCategoryService.updateSubCategory(id, dto);
	    return ResponseEntity.ok(updatedSubCategory);
	}

}
