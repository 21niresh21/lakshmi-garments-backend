package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakshmigarments.dto.request.CategoryRequest;
import com.lakshmigarments.dto.response.CategoryResponse;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.DuplicateCategoryException;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.specification.CategorySpecification;
import com.lakshmigarments.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);

	private final CategoryRepository categoryRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional(readOnly = true)
	public List<CategoryResponse> getAllCategories(String search) {
		LOGGER.debug("Fetching all categories with search criteria: {}", search);
		Specification<Category> spec = CategorySpecification.filterByName(search);

		List<Category> categories = categoryRepository.findAll(spec);
		LOGGER.debug("Found {} category(s) matching filter", categories.size());
		return categories.stream()
				.map(category -> modelMapper.map(category, CategoryResponse.class))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public CategoryResponse createCategory(CategoryRequest categoryRequest) {
		LOGGER.debug("Creating category: {}", categoryRequest.getName());

		String categoryName = categoryRequest.getName().trim();
		String categoryCode = categoryRequest.getCode().trim();

		validateCategoryUniqueness(categoryName, categoryCode, null);

		Category category = new Category();
		category.setName(categoryName);
		category.setCode(categoryCode);

		Category savedCategory = categoryRepository.save(category);
		LOGGER.info("Category created successfully with ID: {}", savedCategory.getId());
		return modelMapper.map(savedCategory, CategoryResponse.class);
	}

	@Override
	@Transactional
	public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
		LOGGER.debug("Updating category with ID: {} to {}", id, categoryRequest.getName());
		
		Category category = this.getCategoryOrThrow(id);
		
		String categoryName = categoryRequest.getName().trim();
		String categoryCode = categoryRequest.getCode().trim();

		validateCategoryUniqueness(categoryName, categoryCode, id);

		category.setName(categoryName);
		category.setCode(categoryCode);
		
		Category updatedCategory = categoryRepository.save(category);
		LOGGER.info("Category updated successfully with ID: {}", updatedCategory.getId());
		return modelMapper.map(updatedCategory, CategoryResponse.class);
	}

	private void validateCategoryUniqueness(String name, String code, Long id) {
		if (id == null) {
			if (categoryRepository.existsByNameIgnoreCase(name)) {
				LOGGER.error("Category name already exists: {}", name);
				throw new DuplicateCategoryException("Category already exists with name: " + name);
			}
			if (categoryRepository.existsByCodeIgnoreCase(code)) {
				LOGGER.error("Category code already exists: {}", code);
				throw new DuplicateCategoryException("Category already exists with code: " + code);
			}
		} else {
			if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
				LOGGER.error("Category name already exists for another ID: {}", name);
				throw new DuplicateCategoryException("Category already exists with name: " + name);
			}
			if (categoryRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
				LOGGER.error("Category code already exists for another ID: {}", code);
				throw new DuplicateCategoryException("Category already exists with code: " + code);
			}
		}
	}

	private Category getCategoryOrThrow(Long id) {
		return categoryRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Category not found with ID: {}", id);
			return new CategoryNotFoundException("Category not found with ID: " + id);
		});
	}

}
