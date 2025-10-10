package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.SubCategoryRequestDTO;
import com.lakshmigarments.dto.SubCategoryResponseDTO;
import com.lakshmigarments.service.SubCategoryService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/sub-categories")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SubCategoryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubCategoryController.class);
	private final SubCategoryService subCategoryService;

	@PostMapping
	public ResponseEntity<SubCategoryResponseDTO> createSubCategory(
			@RequestBody @Valid SubCategoryRequestDTO createSubCategoryDTO) {
		LOGGER.info("Received request to create a new sub category: {}", createSubCategoryDTO.getName());

		SubCategoryResponseDTO subCategoryResponseDTO = subCategoryService.createSubCategory(createSubCategoryDTO);

		LOGGER.info("Sub category created successfully with ID: {}", subCategoryResponseDTO.getId());

		return new ResponseEntity<>(subCategoryResponseDTO, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<SubCategoryResponseDTO>> getAllSubCategories(
			@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all sub categories with search: {}", search);
		List<SubCategoryResponseDTO> subCategories = subCategoryService.getAllSubCategories(search);
		LOGGER.info("Returning {} sub category(s)", subCategories.size());
		return ResponseEntity.ok(subCategories);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<SubCategoryResponseDTO> updateSubCategory(@PathVariable Long id,
			@RequestBody @Valid SubCategoryRequestDTO dto) {

		LOGGER.info("Received request to update sub category with ID: {}", id);
		SubCategoryResponseDTO updatedSubCategory = subCategoryService.updateSubCategory(id, dto);
		LOGGER.info("Sub category updated successfully with ID: {}", id);
		return ResponseEntity.ok(updatedSubCategory);
	}

}
