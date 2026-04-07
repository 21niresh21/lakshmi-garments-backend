package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakshmigarments.dto.request.SubCategoryRequest;
import com.lakshmigarments.dto.response.SubCategoryResponse;
import com.lakshmigarments.exception.DuplicateSubCategoryException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.specification.SubCategorySpecification;
import com.lakshmigarments.service.SubCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubCategoryServiceImpl.class);

	private final SubCategoryRepository subCategoryRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional(readOnly = true)
	public List<SubCategoryResponse> getAllSubCategories(String search) {
		LOGGER.debug("Fetching all subcategories matching criteria: {}", search);
		Specification<SubCategory> spec = SubCategorySpecification.filterByName(search);

		List<SubCategory> subCategories = subCategoryRepository.findAll(spec);
		LOGGER.debug("Found {} subcategory(s) matching search {}", subCategories.size(), search);

		return subCategories.stream()
				.map(subCategory -> modelMapper.map(subCategory, SubCategoryResponse.class))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public SubCategoryResponse createSubCategory(SubCategoryRequest subCategoryRequest) {
		LOGGER.debug("Creating a new subcategory: {}", subCategoryRequest.getName());
		String subCategoryName = subCategoryRequest.getName().trim();

		validateSubCategoryUniqueness(subCategoryName, null);

		SubCategory subCategory = new SubCategory();
		subCategory.setName(subCategoryName);

		SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
		LOGGER.info("Subcategory created with ID: {}", savedSubCategory.getId());
		return modelMapper.map(savedSubCategory, SubCategoryResponse.class);
	}

	@Override
	@Transactional
	public SubCategoryResponse updateSubCategory(Long id, SubCategoryRequest subCategoryRequest) {
		LOGGER.debug("Updating subcategory with ID: {}", id);
		SubCategory subCategory = this.getSubCategoryOrThrow(id);

		String subCategoryName = subCategoryRequest.getName().trim();

		validateSubCategoryUniqueness(subCategoryName, id);

		subCategory.setName(subCategoryName);

		SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
		LOGGER.info("Subcategory updated successfully with ID: {}", savedSubCategory.getId());
		return modelMapper.map(savedSubCategory, SubCategoryResponse.class);
	}

	private void validateSubCategoryUniqueness(String name, Long id) {
		if (id == null) {
			if (subCategoryRepository.existsByNameIgnoreCase(name)) {
				LOGGER.error("Subcategory name already exists: {}", name);
				throw new DuplicateSubCategoryException("Sub category already exists with name: " + name);
			}
		} else {
			if (subCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
				LOGGER.error("Subcategory name already exists for another ID: {}", name);
				throw new DuplicateSubCategoryException("Sub category already exists with name: " + name);
			}
		}
	}

	private SubCategory getSubCategoryOrThrow(Long id) {
		return subCategoryRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Subcategory with ID {} not found", id);
			return new SubCategoryNotFoundException("Sub category not found with ID: " + id);
		});
	}

}
